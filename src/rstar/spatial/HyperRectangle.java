package rstar.spatial;

import rstar.dto.MbrDTO;
import rstar.interfaces.IDtoConvertible;
import util.Constants;

/**
 * User: Lokesh
 * Date: 3/4/12
 * Time: 1:52 AM
 */
public class HyperRectangle implements IDtoConvertible {
    private int _dimension;
    /**
     * points is a 2D double array containing
     * the max and min values for each dimension
     * in the rectangle.
     */
    private float[][] points;
    private static int MAX_CORD = 0;
    private static int MIN_CORD = 1;

    public float[][] getPoints() {
        return points;
    }

    public void setPoints(float[][] points) {
        this.points = points;
    }

    public HyperRectangle(int dimension) {
        this._dimension = dimension;
        points = new float[dimension][2];
    }

    public HyperRectangle(int dimension, SpatialPoint[] points) {
        this._dimension = dimension;
        this.points = new float[dimension][2];

        update(points);
    }

    public HyperRectangle(MbrDTO dto) {
        this._dimension = Constants.DIMENSION;
        this.points = dto.points;
    }

    public HyperRectangle(float[] cords) {
        this._dimension = cords.length;
        points = new float[_dimension][2];
        for (int i = 0; i < _dimension; i++) {
            points[i][MAX_CORD] = cords[i];
            points[i][MIN_CORD] = cords[i];
        }
    }

    private void update(SpatialPoint[] newPoints) {
        for (int j = 0; j < newPoints.length; j++) {
            float[] cord = newPoints[j].getCords();
            assert cord.length == _dimension;
            for (int i = 0; i < cord.length; i++) {
                if (points[i][MAX_CORD] < cord[i]) {
                    points[i][MAX_CORD] = cord[i];
                }
                if (points[i][MIN_CORD] > cord[i]) {
                    points[i][MIN_CORD] = cord[i];
                }
            }
        }
    }

    public void update(SpatialPoint newPoint) {
        SpatialPoint[] newPoints = new SpatialPoint[1];
        newPoints[0] = newPoint;
        update(newPoints);
    }

    public void update(HyperRectangle addedRegion) {
        float[][] newPoints = addedRegion.getPoints();
        assert newPoints.length == _dimension;
        for (int j = 0; j < _dimension; j++) {
            if (points[j][MAX_CORD] < newPoints[j][MAX_CORD]) {
                points[j][MAX_CORD] = newPoints[j][MAX_CORD];
            }
            if (points[j][MIN_CORD] > newPoints[j][MIN_CORD]) {
                points[j][MIN_CORD] = newPoints[j][MIN_CORD];
            }
        }
    }

    /**
     * finds the intersecting region of this MBR with otherMBR
     * @param otherMBR
     * @return the intersecting region, null if not intersecting
     */
    public HyperRectangle getIntersection(HyperRectangle otherMBR) {
        float[][] interPoints = new float[_dimension][2];
        float[][] newPoints = otherMBR.getPoints();
        assert newPoints.length == _dimension;

        boolean intersectExists = true;
        for (int i = 0; i < _dimension; i++) {
            if ((points[i][MAX_CORD] <= newPoints[i][MIN_CORD]) || (points[i][MIN_CORD] >= newPoints[i][MAX_CORD])) {
                intersectExists = false;
                break;
            }
            interPoints[i][MAX_CORD] = Math.min(newPoints[i][MAX_CORD], points[i][MAX_CORD]);
            interPoints[i][MIN_CORD] = Math.max(newPoints[i][MIN_CORD], points[i][MAX_CORD]);
        }

        if (!intersectExists) {
            return null;
        }
        HyperRectangle intersect = new HyperRectangle(_dimension);
        intersect.setPoints(interPoints);
        return intersect;
    }

    public double deltaV_onInclusion(HyperRectangle newmbr) {
        HyperRectangle tempMbr = new HyperRectangle(_dimension);
        tempMbr.setPoints(points);
        tempMbr.update(newmbr);

        return tempMbr.volume() - this.volume();
    }

    /**
     * Computes the volume of this MBR.
     *
     * @return the volume of this MBR
     */
    public double volume() {
        double vol = 1;
        for (int i = 0; i < points.length; i++) {
            vol *= points[i][MAX_CORD] - points[i][MIN_CORD];
        }
        return vol;
    }

    /**
     * Computes the perimeter of this MBR.
     *
     * @return the perimeter of this MBR
     */
    public double perimeter() {
        double perimeter = 0;
        for (int i = 0; i < points.length; i++) {
            perimeter += points[i][MAX_CORD] - points[i][MIN_CORD];
        }
        return perimeter;
    }

    /**
     * Computes the volume of the overlapping box between this MBR and the given MBR
     * and return the relation between the volume of the overlapping box and the volume of both MBRs.
     *
     * @param mbr the MBR for which the intersection volume with this MBR should be computed
     * @return the relation between the volume of the overlapping box and the volume of this MBR
     *         and the given MBR
     */
    public double overlapVolume(HyperRectangle mbr) {
        if (this._dimension != mbr._dimension)
            throw new IllegalArgumentException("This MBR and the given MBR need same dimensionality");

        float[][] otherPoints = mbr.getPoints();
        // the maximal and minimal value of the overlap box.
        float omax, omin;

        // the overlap volume
        double overlap = 1.0;

        for (int i = 0; i < points.length; i++) {
            // The maximal value of that overlap box in the current
            // dimension is the minimum of the max values.
            omax = Math.min(points[i][MAX_CORD], otherPoints[i][MAX_CORD]);
            // The minimal value is the maximum of the min values.
            omin = Math.max(points[i][MIN_CORD], otherPoints[i][MIN_CORD]);

            // if omax <= omin in any dimension, the overlap box has a volume of zero
            if (omax <= omin) {
                return 0.0;
            }

            overlap *= omax - omin;
        }

        return overlap;
    }

    /**
     * Computes the union MBR of this MBR and the given MBR.
     *
     * @param mbr the MBR to be united with this MBR
     * @return the union MBR of this MBR and the given MBR
     */
    public HyperRectangle union(HyperRectangle mbr) {
        if (this._dimension != mbr._dimension)
            throw new IllegalArgumentException("This MBR and the given MBR need same dimensionality");

        float[][] otherPoints = mbr.getPoints();
        float[][] unionPoints = new float[_dimension][2];

        for (int i = 0; i < this._dimension; i++) {
            unionPoints[i][MIN_CORD] = Math.min(this.points[i][MIN_CORD], otherPoints[i][MIN_CORD]);
            unionPoints[i][MAX_CORD] = Math.max(this.points[i][MAX_CORD], otherPoints[i][MAX_CORD]);
        }
        HyperRectangle union = new HyperRectangle(_dimension);
        union.setPoints(unionPoints);
        return union;
    }

    @Override
    public MbrDTO toDTO() {
        return new MbrDTO(points);
    }
}
