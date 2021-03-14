/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import javafx.scene.Node;

public abstract class BaseSongView {

    protected final SongModel songModel;
    private final Node viewNode;

    public BaseSongView(SongModel songModel) {
        this.songModel = songModel;
        viewNode = initView();
    }

    public Node getViewNode() {
        return viewNode;
    }

    protected abstract Node initView();
}
