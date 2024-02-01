import ast
import sys

from skspatial.objects import Plane


def main():
    input_string = sys.argv[1]

    # Convert the string to a list
    points_list = ast.literal_eval(input_string)
    # Reshape the list into sublists of three elements each
    points = [points_list[i:i + 3] for i in range(0, len(points_list), 3)]
    plane = Plane.best_fit(points)
    normal = plane.normal
    point = plane.point
    print(normal[0], normal[1], normal[2], point[0], point[1], point[2])


if __name__ == "__main__":
    main()
