/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch04;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.Region;

public class ReversiPiece extends Region {

    private ObjectProperty<Owner> owner = new SimpleObjectProperty<>(this, "owner", Owner.NONE);

    public ReversiPiece() {
        owner.addListener((ob, old, value) -> setStyle(getFinaleStyle(value)));
        Reflection reflection = new Reflection();
        reflection.setFraction(1);
        reflection.topOffsetProperty().bind(heightProperty().multiply(-3 / 4.));
        setEffect(reflection);
        setPrefSize(180, 180);
        setMouseTransparent(true);
    }

    public ReversiPiece(Owner owner) {
        this();
        this.owner.setValue(owner);
    }

    public Owner getOwner() {
        return owner.get();
    }

    public ObjectProperty<Owner> ownerProperty() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner.set(owner);
    }

    private String getFinaleStyle(Owner owner2) {
        String style = "";
        switch (owner2) {
            case BLACK:
                style = "-fx-background-color: radial-gradient(radius 100%, white 0, black .6); ";
                break;
            case NONE:
                style = "radius 0; ";
                break;
            case WHITE:
                style = "-fx-background-color: radial-gradient(radius 100%, white .4, gray .9, darkgray 1); ";
                break;
            default:
                break;
        }
        return style + " -fx-background-radius: 1000em; -fx-background-insets: 5;";
    }
}
