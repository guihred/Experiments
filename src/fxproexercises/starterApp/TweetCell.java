/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.starterApp;

import javafx.scene.control.ListCell;

public class TweetCell extends ListCell<Tweet> {

    @Override
    protected void updateItem(Tweet tweet, boolean b) {
        if (tweet != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(tweet.getTimeStamp()).append("]").
                    append(tweet.getAuthor()).append(": ").append(tweet.getTitle());
            setText(sb.toString());
        }
    }
}
