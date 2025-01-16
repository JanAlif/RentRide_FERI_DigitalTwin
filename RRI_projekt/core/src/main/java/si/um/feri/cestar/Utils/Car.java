
package si.um.feri.cestar.Utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Car {
    private Vector3 position;
    private List<Vector3> routePoints;
    private int currentPointIndex;
    private float speed;
    private float tolerance = 5f;
    private ZoomXY beginTile;

    public Car(Geolocation[][] routeCoordinates, ZoomXY beginTile, float speed) {
        this.routePoints = new ArrayList<>();
        this.beginTile = beginTile;
        this.speed = speed;
        this.currentPointIndex = 0;


        for (Geolocation[] segment : routeCoordinates) {
            for (Geolocation geo : segment) {
                Vector2 point2D = MapRasterTiles.getPixelPosition(geo.lat, geo.lng, beginTile.x, beginTile.y);
                routePoints.add(new Vector3(point2D.x, point2D.y, 0));
            }
        }


        if (!routePoints.isEmpty()) {
            this.position = routePoints.get(0).cpy();
        } else {
            throw new IllegalArgumentException("Route coordinates cannot be empty.");
        }
    }

    public void update(float delta) {
        if (currentPointIndex >= routePoints.size() - 1) {
            return;
        }


        Vector3 nextPoint = routePoints.get(currentPointIndex + 1);


        Vector3 direction = nextPoint.cpy().sub(position).nor();


        position.add(direction.scl(speed * delta));


        if (position.dst(nextPoint) <= tolerance) {
            currentPointIndex++;
        }
    }

    public void draw(ShapeRenderer shapeRenderer, Matrix4 projectionMatrix) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.circle(position.x, position.y, 10);

        shapeRenderer.end();
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getDirection() {
        if (currentPointIndex >= routePoints.size() - 1) {
            return new Vector3(0, 1, 0);
        }


        return routePoints.get(currentPointIndex + 1).cpy().sub(position).nor();
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public void setCurrentPointIndex(int index) {
        this.currentPointIndex = index;
    }

    public int getCurrentPointIndex() {
        return currentPointIndex;
    }



}
