package si.um.feri.cestar.Utils;

import com.badlogic.gdx.graphics.Color;

public class Confetti {

    public float x;
    public float y;
    float dx, dy;
    public Color color;

    public Confetti(float x, float y, float dx, float dy, Color color) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.color = color;
    }

    public void update(float delta) {
        x += dx * delta;
        y += dy * delta;


        dy -= 10 * delta;
    }

}
