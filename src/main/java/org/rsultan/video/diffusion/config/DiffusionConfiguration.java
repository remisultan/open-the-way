package org.rsultan.video.diffusion.config;

public abstract class DiffusionConfiguration<T> {

  protected String name;

  protected String address;
  protected int width;
  protected int height;

  public abstract T build();

  public String getAddress() {
    return address;
  }

  public DiffusionConfiguration<T> setAddress(String address) {
    this.address = address;
    return this;
  }

  public int getWidth() {
    return width;
  }

  public DiffusionConfiguration<T> setWidth(int width) {
    this.width = width;
    return this;
  }

  public int getHeight() {
    return height;
  }

  public DiffusionConfiguration<T> setHeight(int height) {
    this.height = height;
    return this;
  }

  public String getName() {
    return name;
  }

  public DiffusionConfiguration<T> setName(String name) {
    this.name = name;
    return this;
  }
}
