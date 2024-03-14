import re
import matplotlib.pyplot as plt

# Function to parse traceroute output and compute average delays
def parse_traceroute(file_path):
    traceroute_results = []
    with open(file_path, 'r') as file:
        lines = file.readlines()
    
    for line in lines:
        match = re.search(r"\d+\s+(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}).*?(\d+\.\d+ ms|\*)\s+(\d+\.\d+ ms|\*)\s+(\d+\.\d+ ms|\*)", line)
        if match:
            ip = match.group(1)  # IP address
            # Extract delays, handle '*' cases
            delays = [float(delay.split()[0]) if delay != '*' else None for delay in match.groups()[1:]]
            # Calculate average delay, ignoring '*' cases
            valid_delays = [delay for delay in delays if delay is not None]
            avg_delay = sum(valid_delays) / len(valid_delays) if valid_delays else None
            traceroute_results.append((ip, avg_delay))  # Append result
    
    return traceroute_results

# Paths to the traceroute output files
file_path_1 = 'traceroute_output_1.txt'
file_path_2 = 'traceroute_output_2.txt'

# Parse the traceroute outputs
traceroute_results_1 = parse_traceroute(file_path_1)
traceroute_results_2 = parse_traceroute(file_path_2)

# Extract IPs and Delays for the first set of results
ips1 = [result[0] for result in traceroute_results_1]
delays1 = [result[1] if result[1] is not None else 0 for result in traceroute_results_1]  # Replace 'None' with '0' for plotting

# Extract IPs and Delays for the second set of results
ips2 = [result[0] for result in traceroute_results_2]
delays2 = [result[1] if result[1] is not None else 0 for result in traceroute_results_2]  # Replace 'None' with '0' for plotting

# Plotting the results
plt.figure(figsize=(10, 6))
plt.plot(ips1, delays1, label='Traceroute 1', marker='o')
plt.plot(ips2, delays2, label='Traceroute 2', marker='x')
plt.xticks(rotation=90)  # Rotate x-axis labels for better readability
plt.xlabel('IP Address')
plt.ylabel('Average Delay (ms)')
plt.title('Average Delay per Hop at Two Different Times')
plt.legend()
plt.tight_layout()  # Adjust layout to accommodate x-axis labels
plt.show()
