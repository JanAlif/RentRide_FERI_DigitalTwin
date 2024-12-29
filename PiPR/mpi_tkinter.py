from mpi4py import MPI
import tkinter as tk

# Initialize MPI
comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

# Display rank and size for debugging
print(f"Process {rank + 1}/{size} is running...")

# Finalize MPI to decouple processes
MPI.Finalize()

# Create a GUI directly
root = tk.Tk()
root.title(f"GUI Instance {rank + 1} of {size}")

label = tk.Label(
    root,
    text=f"This is GUI instance {rank + 1} of {size}",
    font=("Arial", 14)
)
label.pack(pady=20)

exit_button = tk.Button(
    root,
    text="Exit",
    command=root.destroy  # Closes the GUI window
)
exit_button.pack(pady=10)

# Run the Tkinter main loop
root.mainloop()