package org.example;

public class CarClass implements CarInterface {

    private Integer engine;

    public Integer wheels;

    public CarClass() {

    }

    public CarClass(Object engine) {

    }

    public CarClass(Integer engine, Integer wheels) {
        this.engine = engine;
        this.wheels = wheels;
    }

    public void drive(Integer s) {
        System.out.println(engine.floatValue());
    }

    private void repair() {

    }

    @Override
    public String toString() {
        return "Engine: " + engine + ", wheels: " + wheels;
    }

    @Override
    public void drive() {

    }

    @Override
    public void beep() {

    }

    @Override
    public void check() {

    }

    @Override
    public Integer getEngine() {
        return engine != null ? engine : 1;
    }

    @Override
    public void setEngine(Integer engine) {
        if (engine > 0 && engine < 10) {
            this.engine = engine;
        }
    }
}
