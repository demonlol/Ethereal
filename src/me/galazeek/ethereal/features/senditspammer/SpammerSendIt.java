package me.galazeek.ethereal.features.senditspammer;

import java.net.MalformedURLException;
import java.net.URL;

public class SpammerSendIt {

    public SpammerSendIt() {

    }

    public void getLinkData(String strLink) {
        try {
            URL link = new URL(strLink);
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL: " + strLink);
        }

        //stickerId = link uuid (question id?)
        //userid = prob sendit user acc
        //shadow token = prob us (believe we can randomize this)

//https://reply.getsendit.com/api/v1/sendpost
/*
{
  "data": {
    "postType": "sendit.post-type:question-and-answer-v1",
    "userId": "17d56aa0-d181-41d4-932b-29a7265c1b15",
    "stickerId": "7a504205-642a-493e-b4d5-d5c0d005cee8",
    "shadowToken": "2a8f6d05-3f03-4fbb-9014-74920d426c5e",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
  },
  "replyData": {
    "question": "how big yo dic",
    "promptText": ""
  }
}
*/
    }

}
