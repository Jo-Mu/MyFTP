# MyFTP
A simple FTP Client that can connect to remote servers


Can performm the following functions:

login to the remot server

ls - lists the files in the current directory on the remote server

cd [remote-dir] - changes the current directory to "remote-dir" on the remote server

get [remote-file] - downloads the file "remote-file" from the remote server to the local machine

put [local-file] - uploads the file "local-file" from the local machine to the remote server

delete - Delete the file "remote-file" from the remote server

quit - disconnects from the remote server and quits the FTP client

# Compilation Instructions

javac myftp.java myftpclient.java

java myftp
