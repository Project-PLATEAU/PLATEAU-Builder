import sys

from Geometry3D import *


def main():
    # create polygon 1
    global type
    pos_list_input_1 = sys.argv[1].replace("[", "").replace("]", "")
    point_sequence1 = tuple(map(float, pos_list_input_1.split(",")))
    sequence_1 = [Point(*point_sequence1[i:i + 3]) for i in range(0, len(point_sequence1), 3)]
    polygon1 = ConvexPolygon(sequence_1)

    # create polygon 2
    pos_list_input_2 = sys.argv[2].replace("[", "").replace("]", "")
    point_sequence2 = tuple(map(float, pos_list_input_2.split(",")))
    sequence_2 = [Point(*point_sequence2[i:i + 3]) for i in range(0, len(point_sequence2), 3)]
    polygon2 = ConvexPolygon(sequence_2)

    point_intersect = intersection(polygon1, polygon2)
    type_intersect = str(type(point_intersect))
    if type_intersect is None:
        # do not intersect
        print("1")
        return
    elif "segment" in type_intersect:
        if (point_intersect[0] in sequence_1 or point_intersect[0] in sequence_2) and (
                point_intersect[1] in sequence_1 or point_intersect[1] in sequence_2):
            # touch
            print("2")
            return
        else:
            # intersect
            print("3")
            return
    elif "point" in type_intersect:
        print("2")
        return
    else:
        # flat and intersect
        print("4")
        return


if __name__ == "__main__":
    main()