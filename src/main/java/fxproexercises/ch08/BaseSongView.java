/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch08;

import javafx.scene.Node;

public abstract class BaseSongView {

    protected final SongModel songModel;
    protected final Node viewNode = initView();

    public BaseSongView(SongModel songModel) {
        this.songModel = songModel;
    }

    public Node getViewNode() {
        return viewNode;
    }

    protected abstract Node initView();
}
