import matplotlib.pyplot as plt

# Example data
time = [7.694299, 4.964848, 3.993738, 3.984321, 3.795126]  # Time (s)
operations = [7484877, 10857057, 11298346, 13232796, 12578162]  # Number of operations
threads = [1, 2, 4, 8, 11]  # Number of threads

# Calculate speed-up for time and operations
speedup_time = [time[0] / t for t in time]  # Speed-up for time
speedup_operations = [operations[i] / operations[0] for i in range(len(operations))]  # Relative scale for operations

# Create a figure with four subplots
plt.figure(figsize=(12, 10))

# Plot 1: Shorter time based on threads
plt.subplot(2, 2, 1)
plt.plot(time, threads, marker='o', linestyle='-')
plt.xlabel("Time (s)")
plt.ylabel("Number of Threads")
plt.title("Threads vs Time")
plt.grid(True)

# Plot 2: More operations based on threads
plt.subplot(2, 2, 2)
plt.plot(operations, threads, marker='o', linestyle='-')
plt.xlabel("Number of Operations")
plt.ylabel("Number of Threads")
plt.title("Threads vs Operations")
plt.grid(True)

# Plot 3: Speed-up for time based on threads
plt.subplot(2, 2, 3)
plt.plot(threads, speedup_time, marker='o', linestyle='-')
plt.xlabel("Number of Threads")
plt.ylabel("Speed-Up (Time)")
plt.title("Speed-Up vs Threads (Time)")
plt.grid(True)

# Plot 4: Speed-up for operations based on threads
plt.subplot(2, 2, 4)
plt.plot(threads, speedup_operations, marker='o', linestyle='-')
plt.xlabel("Number of Threads")
plt.ylabel("Speed-Up (Operations)")
plt.title("Speed-Up vs Threads (Operations)")
plt.grid(True)

# Adjust layout and display the plots
plt.tight_layout()
plt.show()

# 1 node time = 60.22 | 94.39
# 2 node time = 46.78 | 51.26
# 3 node time = 30.51 | 44.75