import matplotlib.pyplot as plt

# Number of nodes
nodes = [1, 2, 3]

# Time taken to mine 10 blocks (in seconds)
time = [94.39, 51.26, 44.75]

# Create a figure and axis
plt.figure(figsize=(8, 6))

# Create a bar chart
bars = plt.bar(nodes, time, color='skyblue', edgecolor='black')

# Adding labels and title
plt.xlabel('Number of Nodes', fontsize=12)
plt.ylabel('Time to Mine 10 Blocks (s)', fontsize=12)
plt.title('Mining Time for 10 Blocks with Different Number of Nodes', fontsize=14)

# Setting x-axis ticks to match the number of nodes
plt.xticks(nodes, [f'{node} Node' if node == 1 else f'{node} Nodes' for node in nodes])

# Adding grid lines for better readability (horizontal grid lines only)
plt.grid(axis='y', linestyle='--', alpha=0.7)

# Adding data labels on top of each bar
for bar in bars:
    height = bar.get_height()
    plt.text(
        bar.get_x() + bar.get_width() / 2,  # X-coordinate
        height + 1,                        # Y-coordinate (slightly above the bar)
        f'{height:.2f}',                   # Text to display
        ha='center', va='bottom', fontsize=10, fontweight='bold'
    )

# Adjust layout to prevent clipping of labels
plt.tight_layout()

# Display the plot
plt.show()