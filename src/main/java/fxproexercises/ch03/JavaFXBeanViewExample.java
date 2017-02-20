package fxproexercises.ch03;

public class JavaFXBeanViewExample {

    private JavaFXBeanModelExample model;

    public JavaFXBeanViewExample(JavaFXBeanModelExample model) {
        this.model = model;
        hookupChangeListeners();
    }

    private void hookupChangeListeners() {
		model.iProperty().addListener((observableValue, oldValue, newValue) -> System.out
				.println("Property i changed: old value = " + oldValue + ", new value =" + newValue));
		model.strProperty().addListener((observableValue, oldValue, newValue) -> System.out
				.println("Property str changed: old value = " + oldValue + ", new value =" + newValue));
		model.colorProperty().addListener((observableValue, oldValue, newValue) -> System.out
				.println("Property color changed: old value = " + oldValue + ",new value =" + newValue));
    }
}
