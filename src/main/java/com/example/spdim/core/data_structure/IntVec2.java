package com.example.spdim.core.data_structure;

import java.util.Objects;

public class IntVec2 {
    private int x;
    private int y;

    public IntVec2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntVec2 intVec2 = (IntVec2) o;
        return (x == intVec2.x) && (y == intVec2.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "IntVec2{" + "x=" + x + ", y=" + y + '}';
    }
}
