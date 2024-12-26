from mpi4py import MPI
import threading
import time

def thread_task(task_id, rank):
    print(f"Thread {task_id} in process {rank} starting")
    time.sleep(2)
    print(f"Thread {task_id} in process {rank} finished")

# Initialize MPI
comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

# Create threads in each MPI process
threads = []
num_threads = 2  # Example: 2 threads per process

for i in range(num_threads):
    t = threading.Thread(target=thread_task, args=(i, rank))
    threads.append(t)
    t.start()

# Wait for all threads to finish
for t in threads:
    t.join()

print(f"Process {rank} completed its threads.")