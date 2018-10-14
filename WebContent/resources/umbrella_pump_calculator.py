# umbrella_pump_calculator.py
# Python 3.6.2

import math
import sys

console_detail = 3

def console_print(string, detail):
    if (detail <= console_detail):
        print(string)

def input_number(prompt):
    while(True):
        inputStr = input(prompt)
        try:
            return int(inputStr)
        except ValueError:
            print("Error: Input is not an integer.")

def input_bool(prompt):
    while(True):
        inputStr = input(prompt + " [Y/N]")
        if (inputStr.lower() == "y" or inputStr.lower() == "yes"):
            return True
        elif (inputStr.lower() == "n" or inputStr.lower() == "no"):
            return False
        else:
            print("Error: Input should be either Y or N.")

def array_add_row(arr, width):
    new_row = []
    for x in range(0, width):
        new_row.append("null")
    arr.append(new_row)

def jump(regular_frames, max_pumps):
    # The width of the array:
    #   1 column for ledge fall off
    #   1 or 2 columns for jump or double jump
    #   the rest for the umbrella dips
    width = 3 + max_pumps
    # Create the empty array
    frames = []
    # Populate the array with velocities
    for i in range(0, regular_frames):
        x = -1
        y = 0
        while True:
            # Scan left-to-right
            x += 1
            # When reaching the end of a row move to the beginning of the next
            if x >= width:
                x = 0
                y += 1
            if y >= len(frames):
                array_add_row(frames, width)
            # Skip cells that already have a velocity
            if frames[y][x] != "null":
                continue
            # Column 0: Ledge fall off
            if x == 0:
                if ignore_ledge_fall:
                    continue
                if y < 21 or y > 27:
                    # Too early to line up with the regular jump values
                    # or too late to avoid being a double jump
                    continue
                elif y == 21:
                    value = 0
                    break
                else:
                    value = frames[y-1][x] + 23
                    break
            # Columns 1 and 2: Jump and double jump
            elif x < 3:
                if x == 2 and not double_jump:
                    continue
                elif y == 0:
                    value = -477
                    break
                else:
                    value = frames[y-1][x] + 23
                    break
            # Columns 3+: Umbrella dips
            else:
                if y < 26:
                    continue
                elif y == 26:
                    # Avoid multiple one-frame umbrella pumps
                    already_a_one_frame_pump = False
                    for u in range(3,x):
                        if (
                            frames[y][u] == 126 and
                            (
                                len(frames) <= y+1 or
                                frames[y+1][u] == "null"
                            )
                        ):
                            already_a_one_frame_pump = True
                            break
                    if already_a_one_frame_pump:
                        continue
                    else:
                        value = 126
                        break
                else:
                    value = frames[y-1][x] + 23
                    break
        # Terminal velocity
        if value > 700:
            value = 700
        # Add that value to the array
        frames[y][x] = value
        # A 138 + 144 can be replaced with an extra 126 + 149 umbrella pump
        if not ignore_ledge_fall and x == 1 and y == 27:
            for u in range(3,width):
                if frames[26][u] == "null":
                    frames[27][0] = "null"
                    frames[27][1] = "null"
                    frames[26][u] = 126
                    frames[27][u-1] = 149
                    break
        # If there are no 138's, then we replace 144 + 144 instead
        if ignore_ledge_fall and x == 2 and y == 27:
            for u in range(3,width):
                if frames[26][u] == "null":
                    frames[27][1] = "null"
                    frames[27][2] = "null"
                    frames[26][u] = 126
                    frames[27][u-1] = 149
                    break
    return frames

def print_array(arr):
    for row in arr:
        for cell in row:
            if cell == "null":
                print(".\t", end='')
            else:
                print(str(cell) + "\t", end='')
        print()

def jump_displacement(frames):
    displacement = 0
    for row in frames:
        for cell in row:
            if cell != "null":
                displacement += cell
    return displacement


################################################################################


# Collect information from the user
console_detail = input_number("How detailed do you want the" +
                              "printouts to be? (0, 1, 2, or 3)")
ignore_ledge_fall = input_bool("Start the jump from a wall? " +
                                "(Ignore the first ledge fall off)")
double_jump = input_bool("Does the user have the double jump powerup?")
gap_x = input_number("Enter the horizontal distance to cover (in centipixels)")
gap_y = input_number("Enter the vertical distance to cover (in centipixels)")
print()

# Begin testing options
console_print("Testing for options", 1)
jump_is_possible = False
best_backup = [999999]
# The maximum number of frames we could spend in umbrella is gap_x/260
for umbrella_frames in range(0, int(gap_x / 260)):
    console_print("Testing " + str(umbrella_frames) + " umbrella frames", 2)
    
    # Calculate preliminary values
    max_pumps = int(umbrella_frames / 2)
        # Integer truncation means that for an odd number of
        # umbrella frames the last pump will be 3-frames long
    console_print("\tUmbrella pumps: " + str(max_pumps), 3)
    umbrella_distance = umbrella_frames * 260
    console_print("\tHorizontal distance travelled in umbrella: "
                  + str(umbrella_distance), 3)
    regular_distance = gap_x - umbrella_distance
    console_print("\tHorizontal distance travelled in freefall: "
                  + str(regular_distance), 3)
    regular_frames = math.ceil(regular_distance / 350)
        # Integer truncation will make this one frame short
        # of the end, but that's okay because the y-velocity
        # of the last frame is irrelevant
    console_print("\tFrames spent in freefall: " + str(regular_frames), 3)
    
    # Create an optimized jump (using PED)
    frames = jump(regular_frames, max_pumps)
    
    # Evaluate the horizontal distance covered by the jump
    displacement_x = 350 * regular_frames + 260 * umbrella_frames
    console_print("\tHorizontal displacement of jump: " + str(displacement_x), 3)
    # Evaluate the vertical distance covered by the jump
    #   the sum of all the freefall velocities from the array
    #   the sum of all the 103 velocity frames from the umbrella frames
    #   22*2 for releasing S before each jump
    displacement_y = jump_displacement(frames) + 103 * umbrella_frames + 44
    console_print("\tVertical displacement of jump: " + str(displacement_y), 3)

    # Test if we end up too far down
    # +200 for the 2 pixel step-up
    if displacement_y <= gap_y + 200:
        # IF not, it's a successful jump
        console_print("\tSuccess", 2)
        print()
        jump_is_possible = True
        break
    else:
        # If so, see how long it would take to climb the wall back up.
        console_print("\tFailed", 2)
        climb_y = displacement_y - (gap_y + 200)
        # Accelerated wall climb speed = 454.5 cpx/f
        adjusted_time = regular_frames + umbrella_frames + (climb_y / 454.5)
        console_print("\tTotal frames including the climb back up: " +
                      str(adjusted_time), 3)
        # If this improves the best wall climb time, replace it
        if adjusted_time < best_backup[0]:
            best_backup = [
                    adjusted_time,
                    frames,
                    umbrella_frames,
                    displacement_x,
                    displacement_y
            ]

# Verify that we found a jump that works
if not jump_is_possible:
    print("Could not find a number of umbrella pumps sufficient to cross the gap.")
    print("Showing the best option, including a wall climb at the end.")
    frames = best_backup[1]
    umbrella_frames = best_backup[2]
    displacement_x = best_backup[3]
    displacement_y = best_backup[4]

# Print the results
print("Umbrella frames:\t" + str(umbrella_frames))
print("Final displcement:\t" + str(displacement_x) + ", " + str(displacement_y))
print()
print_array(frames)
print()

# Print the sequence of pixels and velocities
if not input_bool("Do you wish to see a table of the positions " +
                  "and velocities attained over the jump sequence?"):
    sys.exit(0)
frame_tracker = input_number("Enter a starting frame number")
last_ground_x = input_number("Enter a starting x position (in pixels)")
last_ground_y = input_number("Enter a starting y position (in pixels)")
print()

width = len(frames[0])
height = len(frames)
dist_x = last_ground_x
dist_y = last_ground_y

print("frame\txvel\tyvel\txpos\typos")
for x in range(0, width):
    if x > 2:
        if frames[26][x] == "null":
            # Beyond the last umbrella pump
            continue
        elif x == width - 1 or frames[26][x + 1] == "null":
            # Last umbrella pump
            pump_size = umbrella_frames - 2*(x - 3)
        else:
            pump_size = 2
        for i in range(0, pump_size):
            print("{:d}\t\t\t{:.2f}\t{:.2f}".format(frame_tracker, dist_x,
                                                    dist_y))
            print("\t260*\t103*")
            dist_x += 2.6
            dist_y += 1.03
            frame_tracker += 1
    for y in range(0, height):
        if x < 2:
            if (
                frames[y][x] != "null" and
                (
                    y == height - 1 or
                    frames[y+1][x] == "null"
                )
            ):
                # Last frame before jump (let go of S)
                frames[y][x] = frames[y][x] + 22
        if frames[y][x] != "null":
            print("{:d}\t\t\t{:.2f}\t{:.2f}".format(frame_tracker, dist_x,
                                                    dist_y))
            print("\t350\t{:d}".format(frames[y][x]))
            dist_x += 3.5
            dist_y += frames[y][x] / 100
            frame_tracker += 1
# The last frame (where y velocity doesn't matter)
print("{:d}\t\t\t{:.2f}\t{:.2f}".format(frame_tracker, dist_x, dist_y))
