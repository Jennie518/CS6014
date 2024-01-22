import re

# 初始化结果列表
traceroute_results = []

# 读取文件
with open('/Users/zhanyijun/Desktop/24Spring/CS6014(network security)/hw1/traceroute_output.txt', 'r') as file:
    lines = file.readlines()

# Re-importing necessary libraries after the reset
import re

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
file_path_1 = '/Users/zhanyijun/Desktop/24Spring/CS6014(network security)/hw1/traceroute_output_1.txt'
file_path_2 = '/Users/zhanyijun/Desktop/24Spring/CS6014(network security)/hw1/traceroute_output_2.txt'

# Parse the traceroute outputs
traceroute_results_1 = parse_traceroute(file_path_1)
traceroute_results_2 = parse_traceroute(file_path_2)

# Writing the results to new files
output_file_path_1 = 'averaged_delays_1.txt'
output_file_path_2 = 'averaged_delays_2.txt'

with open(output_file_path_1, 'w') as file:
    for ip, avg_delay in traceroute_results_1:
        file.write(f"{ip} {avg_delay if avg_delay is not None else 'N/A'}\n")

with open(output_file_path_2, 'w') as file:
    for ip, avg_delay in traceroute_results_2:
        file.write(f"{ip} {avg_delay if avg_delay is not None else 'N/A'}\n")

import matplotlib.pyplot as plt

# 读取文件函数
def read_results(file_path):
    ips = []
    delays = []
    with open(file_path, 'r') as file:
        for line in file:
            parts = line.split()
            ip = parts[0]
            delay = None if parts[1] == 'N/A' else float(parts[1])
            ips.append(ip)
            delays.append(delay)
    return ips, delays

# 读取两个文件的数据
ips1, delays1 = read_results(output_file_path_1)
ips2, delays2 = read_results(output_file_path_2)

# 绘制图表
plt.figure(figsize=(10, 6))
plt.plot(ips1, delays1, label='Traceroute 1', marker='o')
plt.plot(ips2, delays2, label='Traceroute 2', marker='x')
plt.xticks(rotation=90)  # 旋转x轴标签以便于阅读
plt.xlabel('IP Address')
plt.ylabel('Average Delay (ms)')
plt.title('Average Delay per Hop at Two Different Times')
plt.legend()
plt.tight_layout()  # 调整布局
plt.show()
