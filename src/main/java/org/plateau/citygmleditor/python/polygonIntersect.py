import sys

from Geometry3D import *


def main():
    # create polygon 1
    global type
    pos_list_input_1 = sys.argv[1].replace("[", "").replace("]", "")
    point_sequence1 = tuple(map(float, pos_list_input_1.split(",")))
    sequence_1 = [Point(*point_sequence1[i:i + 3]) for i in range(0, len(point_sequence1), 3)]
    polygon1 = ConvexPolygon(sequence_1)
#     print(polygon1)

    # create polygon 2
    pos_list_input_2 = sys.argv[2].replace("[", "").replace("]", "")
    point_sequence2 = tuple(map(float, pos_list_input_2.split(",")))
    sequence_2 = [Point(*point_sequence2[i:i + 3]) for i in range(0, len(point_sequence2), 3)]
    polygon2 = ConvexPolygon(sequence_2)
#     print(polygon2)

    point_intersect = intersection(polygon1, polygon2)
#     print(point_intersect)
    type_intersect = str(type(point_intersect))
    if type_intersect is None:
        print("do not intersect")
        return
    elif "segment" in type_intersect:
        if (point_intersect[0] in sequence_1 or point_intersect[0] in sequence_2) and (
                point_intersect[1] in sequence_1 or point_intersect[1] in sequence_2):
            print("touch")
            return
        else:
            print("intersect")
            return
    elif "point" in type_intersect:
        print("touch")
        return
    else:
        print("intersect")
        return


if __name__ == "__main__":
    main()
