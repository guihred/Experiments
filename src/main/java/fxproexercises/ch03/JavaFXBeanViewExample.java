package fxproexercises.ch03;

import simplebuilder.HasLogging;

public class JavaFXBeanViewExample implements HasLogging {

    private JavaFXBeanModelExample model;

    public JavaFXBeanViewExample(JavaFXBeanModelExample model) {
        this.model = model;
        hookupChangeListeners();
    }

    private void hookupChangeListeners() {
        model.iProperty().addListener((observableValue, oldValue, newValue) -> getLogger()
                .info("Property i changed: old value = {}, new value ={}", oldValue, newValue));
        model.strProperty().addListener((observableValue, oldValue, newValue) -> getLogger()
                .info("Property str changed: old value = {}, new value ={}", oldValue, newValue));
        model.colorProperty().addListener((observableValue, oldValue, newValue) -> getLogger()
                .info("Property color changed: old value = {},new value ={}", oldValue, newValue));
    }
}
