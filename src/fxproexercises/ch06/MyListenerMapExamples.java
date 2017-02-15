package fxproexercises.ch06;

import javafx.collections.MapChangeListener;

class MyListenerMapExamples implements MapChangeListener<String, Integer> {

	@Override
	public void onChanged(Change<? extends String, ? extends Integer> change) {
		System.out.println("\tmap = " + change.getMap());
		System.out.println(prettyPrint(change));
	}
	
	private String prettyPrint(Change<? extends String, ? extends Integer> change) {
		StringBuilder sb = new StringBuilder("\tChange event data:\n");
		sb.append("\t\tWas added: ").append(change.wasAdded()).append("\n");
		sb.append("\t\tWas removed: ").append(change.wasRemoved()).append("\n");
		sb.append("\t\tKey: ").append(change.getKey()).append("\n");
		sb.append("\t\tValue added: ").append(change.getValueAdded()).append("\n");
		sb.append("\t\tValue removed: ").append(change.getValueRemoved()).append("\n");
		return sb.toString();
	}
}