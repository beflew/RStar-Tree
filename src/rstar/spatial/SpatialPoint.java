package rstar.spatial;

import rstar.dto.PointDTO;
import rstar.interfaces.IDtoConvertible;

/**
 * User: Lokesh
 * Date: 3/4/12
 * Time: 2:05 AM
 */
public class SpatialPoint implements IDtoConvertible {
    private int _dimension;
    private float[] _cords;
    private float  _oid;

    public SpatialPoint() {
    }

    public SpatialPoint(int dimension) {
        this._dimension = 0;
        this._oid = -1;
    }

    public SpatialPoint(float[] cords) {
        this._cords = cords;
        this._dimension = cords.length;
        this._oid = -1;
    }

    public SpatialPoint(float[] cords, float oid) {
        this._cords = cords;
        this._dimension = cords.length;
        this._oid = oid;
    }

    public int getDimension(){
        return _dimension;
    }

    public void setCords(float[] data){
        this._cords = data;
    }

    public float[] getCords() {
        return _cords;
    }

    public float getOid() {
        return _oid;
    }

    public void setOid(float oid) {
        this._oid = oid;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[ ");
        for (double cord : _cords) {
            str.append(cord + ", ");
        }
        str.append("]");
        return str.toString();
    }

    @Override
    public PointDTO toDTO() {
        return new PointDTO(_oid, _cords);
    }
}
