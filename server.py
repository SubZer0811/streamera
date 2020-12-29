import socket
import threading
import time
import numpy as np
import cv2
from io import BytesIO

def display_image(bytes):
	frame = cv2.imdecode(np.fromstring(bytes, dtype=np.uint8), cv2.IMREAD_UNCHANGED)
	cv2.imshow("frame", frame)
	cv2.waitKey(1)
	print(time.time(), len(bytes), bytes[:10])

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("192.168.100.6", 50001))

server_socket.listen(5)

cli_sock, ret = server_socket.accept()
print(f"CLIENT JOINED: {cli_sock}")

buf_size = 0
buf = []
SIZE = 1500
first = 0
str_buf = b''

start = time.time()
while True:

	msg = cli_sock.recv(SIZE)
	if(msg):
		str_buf += msg
		# print(len(str_buf))

		# if ffd9 exists:
		pos = str_buf.find(b'\xff\xd9')
		if pos >= 0:
			# call display_image()
			display_image(str_buf[:pos+2])
			# replace str_buf with left over bytes
			str_buf = str_buf[pos+2:]
		# else str_buf = str_buf + msg

server_socket.close()