import socket
import threading
import time
import numpy as np
import cv2
from io import BytesIO

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("192.168.100.6", 50000))

server_socket.listen(5)

cli_sock, ret = server_socket.accept()
print(f"CLIENT JOINED: {cli_sock}")

buf_size = 0
buf = []
WIDTH = 640
HEIGHT = 480
PLANE_SIZE = WIDTH*HEIGHT
SIZE = PLANE_SIZE*3
first = 0
str_buf = b''

start = time.time()
while True:

	
	msg = cli_sock.recv(SIZE)
	if(msg):
		buf_size += len(msg)
		str_buf += msg

		if(buf_size >= SIZE):
			
			buf_size = 0
			
			data = np.fromstring(str_buf[:SIZE], dtype='uint8')
			frame = data.reshape((HEIGHT,WIDTH,3))

			cv2.imshow("recvd", frame)
			cv2.waitKey(1)
			str_buf = str_buf[SIZE:]
			print(time.time() - start)
			start = time.time()

server_socket.close()