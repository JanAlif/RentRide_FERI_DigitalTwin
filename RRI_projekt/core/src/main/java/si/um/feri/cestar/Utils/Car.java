
package si.um.feri.cestar.Utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Car {
    private Vector3 position; // Current position of the car in world coordinates
    private List<Vector3> routePoints; // List of all points along the route
    private int currentPointIndex; // Index of the current point the car is moving towards
    private float speed; // Speed of the car in units per second
    private float tolerance = 5f; // Tolerance to consider the point reached
    private ZoomXY beginTile; // Tile zone for map projection

    public Car(Geolocation[][] routeCoordinates, ZoomXY beginTile, float speed) {
        this.routePoints = new ArrayList<>();
        this.beginTile = beginTile;
        this.speed = speed;
        this.currentPointIndex = 0;

        // Convert all route coordinates to a list of Vector3 positions
        for (Geolocation[] segment : routeCoordinates) {
            for (Geolocation geo : segment) {
                Vector2 point2D = MapRasterTiles.getPixelPosition(geo.lat, geo.lng, beginTile.x, beginTile.y);
                routePoints.add(new Vector3(point2D.x, point2D.y, 0)); // Add the point as a 3D vector
            }
        }

        // Set the starting position at the first route point
        if (!routePoints.isEmpty()) {
            this.position = routePoints.get(0).cpy();
        } else {
            throw new IllegalArgumentException("Route coordinates cannot be empty.");
        }
    }

    public void update(float delta) {
        if (currentPointIndex >= routePoints.size() - 1) {
            return; // No more points to follow
        }

        // Get the next route point
        Vector3 nextPoint = routePoints.get(currentPointIndex + 1);

        // Calculate direction to the next point
        Vector3 direction = nextPoint.cpy().sub(position).nor();

        // Move towards the next point
        position.add(direction.scl(speed * delta));

        // Check if the car has reached the next point
        if (position.dst(nextPoint) <= tolerance) {
            currentPointIndex++;
        }
    }

    public void draw(ShapeRenderer shapeRenderer, Matrix4 projectionMatrix) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw the car as a simple circle
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.circle(position.x, position.y, 10); // Represent the car as a circle

        shapeRenderer.end();
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getDirection() {
        if (currentPointIndex >= routePoints.size() - 1) {
            return new Vector3(0, 1, 0); // Default direction if no points are left
        }

        // Calculate direction to the next point
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
