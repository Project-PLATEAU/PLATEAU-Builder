package org.plateaubuilder.core.io.mesh;

/**
 * 軸の方向を表すクラスです。
 */
public class AxisDirection {
    /**
     * ツールの軸方向を表す定数です。
     */
    public static final AxisDirection TOOL_AXIS_DIRECTION = new AxisDirection(AxisEnum.X, AxisEnum.Z, true);

    private AxisEnum east;
    private AxisEnum west;
    private AxisEnum north;
    private AxisEnum south;
    private AxisEnum up;
    private AxisEnum down;

    /**
     * AxisDirection クラスの新しいインスタンスを初期化します。
     * 
     * @param east          東方向の軸
     * @param up            上方向の軸
     * @param isRightHanded 右手系かどうか
     */
    public AxisDirection(AxisEnum east, AxisEnum up, boolean isRightHanded) {
        this.east = east;
        this.up = up;
        if (isRightHanded) {
            initializeRightHanded();
        } else {
            initializeLeftHanded();
        }
    }

    /**
     * positive X の方角を取得します。
     * 
     * @return positive X の方角
     */
    public DirectionEnum getX() {
        return getDirection(AxisEnum.X);
    }

    /**
     * positive Y の方角を取得します。
     * 
     * @return positive Y の方角
     */
    public DirectionEnum getY() {
        return getDirection(AxisEnum.Y);
    }

    /**
     * positive Z の方角を取得します。
     * 
     * @return positive Z の方角
     */
    public DirectionEnum getZ() {
        return getDirection(AxisEnum.Z);
    }

    /**
     * 指定された方角に対応する軸を取得します。
     * 
     * @param direction 方角
     * @return 方角に対応する軸
     */
    public AxisEnum getAxis(DirectionEnum direction) {
        if (direction == DirectionEnum.EAST) {
            return east;
        } else if (direction == DirectionEnum.WEST) {
            return west;
        } else if (direction == DirectionEnum.NORTH) {
            return north;
        } else if (direction == DirectionEnum.SOUTH) {
            return south;
        } else if (direction == DirectionEnum.UP) {
            return up;
        } else {
            return down;
        }
    }

    private DirectionEnum getDirection(AxisEnum axis) {
        if (axis == east) {
            return DirectionEnum.EAST;
        } else if (axis == west) {
            return DirectionEnum.WEST;
        } else if (axis == north) {
            return DirectionEnum.NORTH;
        } else if (axis == south) {
            return DirectionEnum.SOUTH;
        } else if (axis == up) {
            return DirectionEnum.UP;
        } else {
            return DirectionEnum.DOWN;
        }
    }

    private void initializeRightHanded() {
        switch (this.east) {
            case X:
                this.west = AxisEnum.NEGATIVE_X;
                switch (this.up) {
                    case Z:
                        this.down = AxisEnum.NEGATIVE_Z;
                        this.north = AxisEnum.Y;
                        this.south = AxisEnum.NEGATIVE_Y;
                        break;
                    case Y:
                        this.down = AxisEnum.NEGATIVE_Y;
                        this.north = AxisEnum.NEGATIVE_Z;
                        this.south = AxisEnum.Z;
                        break;
                    case NEGATIVE_Z:
                        this.down = AxisEnum.Z;
                        this.north = AxisEnum.NEGATIVE_Y;
                        this.south = AxisEnum.Y;
                        break;
                    case NEGATIVE_Y:
                        this.down = AxisEnum.Y;
                        this.north = AxisEnum.Z;
                        this.south = AxisEnum.NEGATIVE_Z;
                        break;
                    default:
                    throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case Y:
                this.west = AxisEnum.NEGATIVE_Y;
                switch (this.up) {
                    case X:
                        this.down = AxisEnum.NEGATIVE_X;
                        this.north = AxisEnum.Z;
                        this.south = AxisEnum.NEGATIVE_Z;
                        break;
                    case NEGATIVE_X:
                        this.down = AxisEnum.X;
                        this.north = AxisEnum.NEGATIVE_Z;
                        this.south = AxisEnum.Z;
                        break;
                    case Z:
                        this.down = AxisEnum.NEGATIVE_Z;
                        this.north = AxisEnum.NEGATIVE_X;
                        this.south = AxisEnum.X;
                        break;
                    case NEGATIVE_Z:
                        this.down = AxisEnum.Z;
                        this.north = AxisEnum.X;
                        this.south = AxisEnum.NEGATIVE_X;
                        break;
                    default:
                    throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case Z:
                this.west = AxisEnum.NEGATIVE_Z;
                switch (this.up) {
                    case X:
                        this.down = AxisEnum.NEGATIVE_X;
                        this.north = AxisEnum.NEGATIVE_Y;
                        this.south = AxisEnum.Y;
                        break;
                    case NEGATIVE_X:
                        this.down = AxisEnum.X;
                        this.north = AxisEnum.Y;
                        this.south = AxisEnum.NEGATIVE_Y;
                        break;
                    case Y:
                        this.down = AxisEnum.NEGATIVE_Y;
                        this.north = AxisEnum.X;
                        this.south = AxisEnum.NEGATIVE_X;
                        break;
                    case NEGATIVE_Y:
                        this.down = AxisEnum.Y;
                        this.north = AxisEnum.NEGATIVE_X;
                        this.south = AxisEnum.X;
                        break;
                    default:
                    throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case NEGATIVE_X:
                this.west = AxisEnum.X;
                switch (this.up) {
                    case Z:
                        this.down = AxisEnum.NEGATIVE_Z;
                        this.north = AxisEnum.NEGATIVE_Y;
                        this.south = AxisEnum.Y;
                        break;
                    case Y:
                        this.down = AxisEnum.NEGATIVE_Y;
                        this.north = AxisEnum.Z;
                        this.south = AxisEnum.NEGATIVE_Z;
                        break;
                    case NEGATIVE_Z:
                        this.down = AxisEnum.Z;
                        this.north = AxisEnum.Y;
                        this.south = AxisEnum.NEGATIVE_Y;
                        break;
                    case NEGATIVE_Y:
                        this.down = AxisEnum.Y;
                        this.north = AxisEnum.NEGATIVE_Z;
                        this.south = AxisEnum.Z;
                        break;
                    default:
                    throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case NEGATIVE_Y:
                this.west = AxisEnum.Y;
                switch (this.up) {
                    case X:
                        this.down = AxisEnum.NEGATIVE_X;
                        this.north = AxisEnum.NEGATIVE_Z;
                        this.south = AxisEnum.Z;
                        break;
                    case NEGATIVE_X:
                        this.down = AxisEnum.X;
                        this.north = AxisEnum.Z;
                        this.south = AxisEnum.NEGATIVE_Z;
                        break;
                    case Z:
                        this.down = AxisEnum.NEGATIVE_Z;
                        this.north = AxisEnum.X;
                        this.south = AxisEnum.NEGATIVE_X;
                        break;
                    case NEGATIVE_Z:
                        this.down = AxisEnum.Z;
                        this.north = AxisEnum.NEGATIVE_X;
                        this.south = AxisEnum.X;
                        break;
                    default:
                    throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case NEGATIVE_Z:
                this.west = AxisEnum.Z;
                switch (this.up) {
                    case X:
                        this.down = AxisEnum.NEGATIVE_X;
                        this.north = AxisEnum.Y;
                        this.south = AxisEnum.NEGATIVE_Y;
                        break;
                    case NEGATIVE_X:
                        this.down = AxisEnum.X;
                        this.north = AxisEnum.NEGATIVE_Y;
                        this.south = AxisEnum.Y;
                        break;
                    case Y:
                        this.down = AxisEnum.NEGATIVE_Y;
                        this.north = AxisEnum.NEGATIVE_X;
                        this.south = AxisEnum.X;
                        break;
                    case NEGATIVE_Y:
                        this.down = AxisEnum.Y;
                        this.north = AxisEnum.X;
                        this.south = AxisEnum.NEGATIVE_X;
                        break;
                    default:
                    throw new IllegalArgumentException("Invalid axis");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid axis");
        }
    }

    private void initializeLeftHanded() {
        switch (this.east) {
            case X:
                this.west = AxisEnum.NEGATIVE_X;
                switch (this.up) {
                    case Y:
                        this.down = AxisEnum.NEGATIVE_Y;
                        this.north = AxisEnum.Z;
                        this.south = AxisEnum.NEGATIVE_Z;
                        break;
                    case NEGATIVE_Y:
                        this.down = AxisEnum.Y;
                        this.north = AxisEnum.NEGATIVE_Z;
                        this.south = AxisEnum.Z;
                        break;
                    case Z:
                        this.down = AxisEnum.NEGATIVE_Z;
                        this.north = AxisEnum.NEGATIVE_Y;
                        this.south = AxisEnum.Y;
                        break;
                    case NEGATIVE_Z:
                        this.down = AxisEnum.Z;
                        this.north = AxisEnum.Y;
                        this.south = AxisEnum.NEGATIVE_Y;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case Y:
                this.west = AxisEnum.NEGATIVE_Y;
                switch (this.up) {
                    case X:
                        this.down = AxisEnum.NEGATIVE_X;
                        this.north = AxisEnum.NEGATIVE_Z;
                        this.south = AxisEnum.Z;
                        break;
                    case NEGATIVE_X:
                        this.down = AxisEnum.X;
                        this.north = AxisEnum.Z;
                        this.south = AxisEnum.NEGATIVE_Z;
                        break;
                    case Z:
                        this.down = AxisEnum.NEGATIVE_Z;
                        this.north = AxisEnum.X;
                        this.down = AxisEnum.NEGATIVE_X;
                        break;
                    case NEGATIVE_Z:
                        this.down = AxisEnum.Z;
                        this.north = AxisEnum.NEGATIVE_X;
                        this.south = AxisEnum.X;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case Z:
                this.west = AxisEnum.NEGATIVE_Z;
                switch (this.up) {
                    case X:
                        this.down = AxisEnum.NEGATIVE_X;
                        this.north = AxisEnum.Y;
                        this.south = AxisEnum.NEGATIVE_Y;
                        break;
                    case NEGATIVE_X:
                        this.down = AxisEnum.X;
                        this.north = AxisEnum.NEGATIVE_Y;
                        this.south = AxisEnum.Y;
                        break;
                    case Y:
                        this.down = AxisEnum.NEGATIVE_Y;
                        this.north = AxisEnum.NEGATIVE_X;
                        this.south = AxisEnum.X;
                        break;
                    case NEGATIVE_Y:
                        this.down = AxisEnum.Y;
                        this.north = AxisEnum.X;
                        this.south = AxisEnum.NEGATIVE_X;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case NEGATIVE_X:
                this.west = AxisEnum.X;
                switch (this.up) {
                    case Y:
                        this.down = AxisEnum.NEGATIVE_Y;
                        this.north = AxisEnum.NEGATIVE_Z;
                        this.south = AxisEnum.Z;
                        break;
                    case NEGATIVE_Y:
                        this.down = AxisEnum.Y;
                        this.north = AxisEnum.Z;
                        this.south = AxisEnum.NEGATIVE_Z;
                        break;
                    case Z:
                        this.down = AxisEnum.NEGATIVE_Z;
                        this.north = AxisEnum.Y;
                        this.south = AxisEnum.NEGATIVE_Y;
                        break;
                    case NEGATIVE_Z:
                        this.down = AxisEnum.Z;
                        this.north = AxisEnum.NEGATIVE_Y;
                        this.south = AxisEnum.Y;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case NEGATIVE_Y:
                this.west = AxisEnum.Y;
                switch (this.up) {
                    case X:
                        this.down = AxisEnum.NEGATIVE_X;
                        this.north = AxisEnum.Z;
                        this.south = AxisEnum.NEGATIVE_Z;
                        break;
                    case NEGATIVE_X:
                        this.down = AxisEnum.X;
                        this.north = AxisEnum.NEGATIVE_Z;
                        this.south = AxisEnum.Z;
                        break;
                    case Z:
                        this.down = AxisEnum.NEGATIVE_Z;
                        this.north = AxisEnum.NEGATIVE_X;
                        this.south = AxisEnum.X;
                        break;
                    case NEGATIVE_Z:
                        this.down = AxisEnum.Z;
                        this.north = AxisEnum.X;
                        this.south = AxisEnum.NEGATIVE_X;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid axis");
                }
                break;
            case NEGATIVE_Z:
                this.west = AxisEnum.Z;
                switch (this.up) {
                    case X:
                        this.down = AxisEnum.NEGATIVE_X;
                        this.north = AxisEnum.NEGATIVE_Y;
                        this.south = AxisEnum.Y;
                        break;
                    case NEGATIVE_X:
                        this.down = AxisEnum.X;
                        this.north = AxisEnum.Y;
                        this.south = AxisEnum.NEGATIVE_Y;
                        break;
                    case Y:
                        this.down = AxisEnum.NEGATIVE_Y;
                        this.north = AxisEnum.NEGATIVE_X;
                        this.south = AxisEnum.X;
                        break;
                    case NEGATIVE_Y:
                        this.down = AxisEnum.Y;
                        this.north = AxisEnum.X;
                        this.south = AxisEnum.NEGATIVE_X;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid axis");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid axis");
        }
    }
}