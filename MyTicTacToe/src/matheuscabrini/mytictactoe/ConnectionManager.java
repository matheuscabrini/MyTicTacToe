package matheuscabrini.mytictactoe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// Classe gerenciadora da conexão entre os dois jogadores, responsável por
// criar a conexão e receber/transmitir jogadas
public class ConnectionManager {
	private Socket socket;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	
	// Construtor para gerar uma conexão de HOST
	public ConnectionManager(int port) throws IOException {
		ServerSocket serverSocket = new ServerSocket(port);
		socket = serverSocket.accept();
	    dataIn = new DataInputStream(socket.getInputStream());	
	    dataOut = new DataOutputStream(socket.getOutputStream());
	    serverSocket.close();
	}

	// Construtor para gerar uma conexão de CLIENT
	public ConnectionManager(String IP, int port) throws IOException {
		socket = new Socket(IP, port);
		dataIn = new DataInputStream(socket.getInputStream());	
		dataOut = new DataOutputStream(socket.getOutputStream());
	}
	
	// Envia pelo socket a posição (i,j) de uma jogada
	public void sendMove(int i, int j) {
		try {
			dataOut.writeInt(i);
			dataOut.writeInt(j);
			dataOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Recebe pelo socket a posição (i,j) de uma jogada.
	// Deve ser chamada duas vezes, pois retorna i na primeira
	// e j na segunda vez
	public int receiveMove() {
		int n = -1;
		try {
			n = dataIn.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return n;
	}
	
	// Finalização
	public void cleanUp() {
		try {
			if (dataIn  != null) dataIn.close();
			if (dataOut != null) dataOut.close();
			if (socket  != null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
