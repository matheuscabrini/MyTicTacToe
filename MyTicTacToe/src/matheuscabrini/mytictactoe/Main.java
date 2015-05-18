package matheuscabrini.mytictactoe;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {
	// Variáveis relativas ao funcionamento do jogo: 
	private ConnectionManager connManager;
	private TTTGame game; 
	private String myMark;
	private String enemyMark; 
	private String firstMark = "x"; // x começa primeiro, na primeira partida
	
	// Elementos de interface utilizados em mais de um método:
	private Stage primaryStage;
	private Button[][] buttonGrid; // Conjunto 3x3 de botões do jogo
	private Label connectionLog; // Mostra mensagens relativas ao status da conexão no menu inicial
	private Label gameLog; // Mostra mensagens relativas ao andamento do jogo
	private GridPane gPane; // Agrupa o conjunto de botões e gameLog
			
	public static void main(String[] args) {
		launch(args);
	}
	
	// Esta primeira etapa do programa gerencia o menu inicial,
	// que coleta os dados de conexão do usuário
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("My Tic-Tac-Toe!");
		
		Text logo = new Text("My Tic-Tac-Toe!");
		
		Button bHost = new Button("Host game");
		Button bConn = new Button("Connect to game");
		
		TextField fieldIp = new TextField(); // Coleta o IP desejado
		TextField fieldPort = new TextField(); // Coleta a port desejada
		fieldIp.setPromptText("Enter IP Address: ");
		fieldPort.setPromptText("Enter port number: ");
		fieldIp.setVisible(false);
		fieldPort.setVisible(false);
		
		Button bGo = new Button("Go!");
		Button bBack = new Button("Back");
		HBox hbox = new HBox(bBack, bGo);
		hbox.setVisible(false);
		
		connectionLog = new Label();
		connectionLog.setVisible(false);
		
		VBox vbox = new VBox(logo, bHost, bConn, fieldIp, fieldPort, hbox, connectionLog);
		
		// Criando scene, aplicando-lhe CSS e mostrando-a na tela:
		Scene menuScn = new Scene(vbox, 600, 600);
		primaryStage.setScene(menuScn);
		String css = this.getClass().getResource("menuStyle.css").toExternalForm();
		logo.getStyleClass().add("logo");
		hbox.getStyleClass().add("box");
		vbox.getStyleClass().add("box");
		menuScn.getStylesheets().add(css);
		primaryStage.show();
		
		bHost.setOnMouseClicked(ev1 -> {
			bConn.setVisible(false);
			fieldPort.setVisible(true);
			hbox.setVisible(true);
		});
		
		bConn.setOnMouseClicked(ev2 -> {
			bHost.setVisible(false);
			fieldPort.setVisible(true);
			fieldIp.setVisible(true);
			hbox.setVisible(true);
		});
		
		bGo.setOnMouseClicked(ev3 -> {
			String ip = fieldIp.getText();
		 	int port = Integer.parseInt(fieldPort.getText());
		 	connectionLog.setVisible(true);
		 	setupConnection(ip, port);
		});	
		
		bBack.setOnMouseClicked(ev4 -> {
			bHost.setVisible(true);
			bConn.setVisible(true);
			fieldPort.setVisible(false);
			fieldIp.setVisible(false);
			hbox.setVisible(false);
		 	connectionLog.setVisible(false);
		});
	}
	
	// Este método tenta criar a conexão com os dados digitados pelo
	// usuário. Como o método accept(), de ServerSocket, utilizado por
	// ConnectionManager, é blocking, deve ser rodado em uma Thread
	// diferente da JavaFX Application Thread. Além disso, usa-se Platform.runLater()
	// para realizar operações que modificam a GUI, pois não se deve modificar a GUI
	// em Thread que não seja a da JavaFX Application.
	public void setupConnection(String IP, int port) {
		// Caso em que o usuário queira ser HOST:
		if (IP.isEmpty()) {
			myMark = "x"; // Host recebe a marca x por default
			enemyMark = "o";
			new Thread(() -> {
				try {
					Platform.runLater(() -> {
						connectionLog.setText("Waiting connection...");
					});
					connManager = new ConnectionManager(port);
					Platform.runLater(() -> {
						gameStart();
					});
				} catch (IOException e) {
					e.printStackTrace();
					Platform.runLater(() -> {
						connectionLog.setText("Error! Try again..");
					});
				}
			}).start();
		}
		
		// Caso em que o usuário queira ser CLIENT:
		else {
			myMark = "o";
			enemyMark = "x";
			try {
				connManager = new ConnectionManager(IP, port);
				gameStart();
			} catch (IOException e) {
				e.printStackTrace();
				Platform.runLater(() -> {
					connectionLog.setText("Error! Try again..");
				});
			}
		}
	}
	
	// Aqui, monta-se o cenário do jogo e cria os event handlers para 
	// cada Button no grid 3x3; assim iniciando o jogo.
	public void gameStart() {
		game = new TTTGame(3);

		gameLog = new Label("Your turn! (" + myMark + ")"); // Mensagem default
		gPane = new GridPane(); 
		gPane.add(gameLog, 0, 3, 2, 4); 
		
		Scene gameScn = new Scene(gPane);
		String css = this.getClass().getResource("gameStyle.css").toExternalForm();
		gameScn.getStylesheets().add(css);

		buttonGrid = new Button[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				buttonGrid[i][j] = new Button();
				buttonGrid[i][j].setPrefSize(170, 170);
				gPane.add(buttonGrid[i][j], j, i);
				
				buttonGrid[i][j].setOnMouseClicked(event -> {
					getMyPlay(event);
				});
			}
		}	

		primaryStage.setScene(gameScn);
		
		// Se não for o primeiro jogador, fica esperando
		// a jogada do oponente; se for, o jogo procede no
		// momento em que algum botão seja clicado
		if (myMark != firstMark)
			getEnemyPlay();	
	}
	
	// Este método identifica a jogada a qual quero fazer 
	// com base no event gerado, faz a jogada acontecer com makePlay()
	// e já entra no modo de esperar jogada do oponente, se necessário
	public void getMyPlay(MouseEvent event) {
		Button b = (Button) event.getSource();
		int iClick = GridPane.getRowIndex(b); 
		int jClick = GridPane.getColumnIndex(b); 
		
		connManager.sendMove(iClick, jClick);
		makePlay(myMark, iClick, jClick);
		
		// Se esta jogada não fez o jogo terminar,
		// espera-se pela jogada do oponente
		if (game.getWinner() == TTTGame.NO_MARK) 
			getEnemyPlay();
	}

	// Este método identifica a jogada do oponente (recebida por connManager)
	// dentro de uma Thread separada, pois operação de leitura com socket
	// é blocking, que não combina com a JavaFX Application Thread.
	// Novamente usamos Platform.runLater() para modificar a GUI fora da 
	// Thread principal; então efetivamos a jogada com makePlay().
	public void getEnemyPlay() {
		gameLog.setText("Opponent's turn... (" + enemyMark + ")");
		gPane.setDisable(true); // para que eu não possa mexer no jogo
								// enquanto é a vez do oponente
		
		new Thread(() -> {
			int iEnemy = connManager.receiveMove();
			int jEnemy = connManager.receiveMove();
			Platform.runLater(() -> {
				gPane.setDisable(false);
				gameLog.setText("Your turn! (" + myMark + ")");
				makePlay(enemyMark, iEnemy, jEnemy);
			});
		}).start();
	}
	
	// Este método recebe a marca e posição de alguma jogada e a efetiva;
	// ou seja, escreve na GUI a marca com o CSS adequado, além de atualizar 
	// o objeto do jogo em si com setPlay(). Ao fim, checamos se o jogo deve terminar
	public void makePlay(String mark, int i, int j) {		
		if (mark == "x") {
			game.setPlay(TTTGame.X_MARK, i, j); 
			buttonGrid[i][j].getStyleClass().add("x-button");
		}
		else { 
			game.setPlay(TTTGame.O_MARK, i, j);
			buttonGrid[i][j].getStyleClass().add("o-button");
		}
		
		buttonGrid[i][j].setText(mark);
		buttonGrid[i][j].setDisable(true); // Para que o botão não possa mais ser clicado
		checkEnd();
	}

	// Aqui, com a ajuda do método game.getWinner, decidimos se 
	// o jogo deve finalizar. Caso sim, uma mensagem é mostrada,
	// os botões são desabilitados para não haver mais clicks, além
	// de ser disponibilizada a possibilidade de jogar novamente.
	public void checkEnd() {
		int res = game.getWinner();
		if (res != TTTGame.NO_MARK) {
			if (res == TTTGame.DRAW) 
				gameLog.setText("No one wins! Click HERE to play again.");
			else if (res == TTTGame.X_MARK)
				gameLog.setText("Player (x) wins!!! Click HERE to play again.");
			else if (res == TTTGame.O_MARK)
				gameLog.setText("Player (o) wins!!! Click HERE to play again.");
						
			for (int i = 0; i < 3; i++) 
				for (int j = 0; j < 3; j++) 
					buttonGrid[i][j].setDisable(true);
	
			gameLog.setOnMouseClicked(event -> {
				resetGame();
			});
		}
	 }
	
	// Para jogar novamente: reiniciamos o objeto do jogo; trocamos a
	// marca que joga primeiro; e retiramos marcas e seu CSS de cada
	// botão, além de possibilitar que recebam clicks.
	public void resetGame() {
		game.resetBoard();
		firstMark = (firstMark == "x" ? "o" : "x");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (buttonGrid[i][j].getText() == "x")
					buttonGrid[i][j].getStyleClass().remove("x-button");
				else 
					buttonGrid[i][j].getStyleClass().remove("o-button");
				buttonGrid[i][j].setText("");
				buttonGrid[i][j].setDisable(false);
			}
		}
		
		// Quem não joga primeiro deve receber jogada do oponente;
		// para quem joga, é habilitado seus clicks nos botões.
		if (myMark != firstMark) 
			getEnemyPlay();	
		else {
			gPane.setDisable(false);
			gameLog.setText("Your turn! (" + myMark + ")");
		}
	}
	
	// Este método é chamado pela Application quando o usuário
	// sai do programa pelo X no canto superior.
	@Override
	public void stop() {
		connManager.cleanUp();
	}
}
