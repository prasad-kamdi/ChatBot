package com.pk.cts.blogTelegram;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class App {
	String url 						="https://api.telegram.org/bot";
	String access_token 			="your token key goes here";
	String getMsgMethod 			="/getUpdates";
	String sendReplyMethod 			="/sendMessage?chat_id=";
	static String sender_ID 		= null;
	static String sender_Name 		= null;
	static String sender_Message 	= null;

	public static void main(String[] args) {
		System.out.println(">> My bot started <<");

		App obj = new App();
		StringBuffer messages;
		try {
			// read in coming messages
			messages = obj.read();

			// parse messages and print
			obj.parseMessages(messages);

			// reply to in coming message to sender with Welcome message
			if (sender_ID == null) {
				System.err.println("== Please send incoming message to your bot and try again ==");
			} else {
				obj.sendReply();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private StringBuffer read() throws Exception {
		String urlRead = url + access_token + getMsgMethod;
		HttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(urlRead);
		HttpResponse response = client.execute(request);

		return getMsg(response);
	}

	private StringBuffer getMsg(HttpResponse res) throws Exception {
		StringBuffer retVal = new StringBuffer();
		BufferedReader rd = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retVal.append(line);
		}
		return retVal;
	}

	private void parseMessages(StringBuffer strBuffer) throws Exception {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(strBuffer.toString());
		JSONObject data = (JSONObject) obj;
		JSONArray arr1 = (JSONArray) data.get("result");
		int size = arr1.size();

		if (size == 0) {
			System.err.println("== No incoming message to your bot, try again by sending message to your bot ==");
		} else {
			for (int i = 0; i < size; i++) {
				JSONObject message = (JSONObject) arr1.get(i);
				JSONObject msg = (JSONObject) message.get("message");
				JSONObject chat = (JSONObject) msg.get("chat");

				sender_ID = chat.get("id").toString();
				sender_Name = (String) chat.get("username");
				sender_Message = (String) msg.get("text");

				if (sender_Name == null || "".equals(sender_Name)) {
					sender_Name = (String) chat.get("first_name");
				}

				System.out.println("Incoming message -->" + sender_Message + " from Sender -->" + sender_Name);
			}
		}
	}

	private void sendReply() throws Exception {
		String reply = "! " + getWelcomeMessage() + "from "+ App.class;
		
		String sendUrl = url + access_token + sendReplyMethod + sender_ID + "&text=Hi%20" + sender_Name
				+ URLEncoder.encode(reply, "UTF-8");

		HttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(sendUrl);
		HttpResponse response = client.execute(request);
		System.out.println(
				"Sending message " + reply + " >>>>Response Code : " + response.getStatusLine().getStatusCode());
	}

	private String getWelcomeMessage() {

		String retVal = "";
		Date myDate = new Date();
		int hrs = myDate.getHours();

		/* hour is before noon */
		if (hrs < 12) {
			retVal = " Good Morning ";
		} else /* Hour is from noon to 5pm (actually to 5:59 pm) */
		if (hrs >= 12 && hrs <= 17) {
			retVal = " Good Afternoon ";
		} else /* the hour is after 5pm, so it is between 6pm and midnight */
		if (hrs > 17 && hrs <= 24) {
			retVal = " Good Evening ";
		}
		return retVal;

	}

}
