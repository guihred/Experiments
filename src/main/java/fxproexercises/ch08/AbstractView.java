/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch08;

import javafx.scene.Node;

public abstract class AbstractView {

    protected final SongModel songModel;
    protected final Node viewNode;

    public AbstractView(SongModel songModel) {
        this.songModel = songModel;
        viewNode = initView();
    }

    public Node getViewNode() {
        return viewNode;
    }

	public void setNextHandler() {
    }
    protected abstract Node initView();
}
