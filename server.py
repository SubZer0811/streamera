import socket
import threading
import time
import numpy as np
import cv2
from io import BytesIO
import argparse

def display_image(bytes):
	frame = cv2.imdecode(np.fromstring(bytes, dtype=np.uint8), cv2.IMREAD_UNCHANGED)
	
	'''
	####################
	Do anything you want to do with the frame recvd
	####################
	'''
	
	cv2.imshow("streamera", frame)
	cv2.waitKey(1)

def main(server, port):
	
	try:
		server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		server_socket.bind((server, port))
		print(f'[+] streamera server created at {server}:{port}')
	except:
		print(f'[!] Could not create streamera server at {server}:{port}')
		return -1

	server_socket.listen(5)

	cli_sock, ret = server_socket.accept()
	print(f"[+] CLIENT JOINED: {cli_sock}")

	SIZE = 1500
	str_buf = b''

	while True:
		
		try:
			cli_sock.sendall(b'asdf')
		except:
			print('[-] Client disconnected.\n[-] Terminating server.')
			break

		msg = cli_sock.recv(SIZE)
		if(msg):
			str_buf += msg

			# if string contains ffd9 (it means end of image)
			pos = str_buf.find(b'\xff\xd9')
			if pos >= 0:
				# call display_image()
				display_image(str_buf[:pos+2])
				# replace str_buf with left over bytes
				str_buf = str_buf[pos+2:]

	server_socket.close()

if __name__ == "__main__":

	argparser = argparse.ArgumentParser()
	argparser.add_argument("server", type=str, help="IP Address")
	argparser.add_argument("port", type=int, help="Port")
	args = argparser.parse_args()

	main(args.server, args.port)