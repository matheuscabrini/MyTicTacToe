package matheuscabrini.mytictactoe;

// Esta clase representa o jogo em si, guardando jogadas realizadas e permitindo
// checagem de fim do jogo, bem como reinicialização do mesmo.
public class TTTGame {
	// Constantes que representam elementos do jogo:
	public static final int NO_MARK = 0; 
	public static final int X_MARK = 1;
	public static final int O_MARK = 2;
	public static final int DRAW = 3;
	
	private int[][] board; // Representação do jogo em matriz
	private int playCount; // Contador de jogadas 

	// Variáveis que guardam os dados da última jogada, a fim de otimizar
	// o algoritmo de checagem de fim de jogo (getWinner()):
	private int lastMark;
	private int iLastPlay; 
	private int jLastPlay;
	
	// Cria um jogo size x size.
	public TTTGame(int size) {
		board = new int[size][size];
		resetBoard();
	}
	
	// Limpa o jogo.
	public void resetBoard() {
		playCount = 0;
		for (int i = 0; i < board.length; i++) 
			for (int j = 0; j < board.length; j++) 
				board[i][j] = NO_MARK;
	}
	
	// Recebe a mark a ser colocada na posição (i, j)
	public void setPlay(int mark, int i, int j) {
		if ((mark == X_MARK || mark == O_MARK) &&
		    (i >= 0 && i < board.length) && 
			(j >= 0 && j < board.length) &&
			(board[i][j] == NO_MARK)) {
			
			board[i][j] = mark;
			playCount++;
			lastMark = mark;
			iLastPlay = i;
			jLastPlay = j;
		}
	}
	
	// Retorna: (NO_MARK = 0) caso nenhum jogador ainda tenha ganhado; 
	// número de qual jogador ganhou, X (X_MARK = 1) ou O (O_MARK = 2); 
	// (DRAW = 3) caso tenha empate.
	public int getWinner() {
		// Número minimo de jogadas para ser possível alguém ganhar:
		if (playCount < 2*board.length - 1)
			return 0;
		
    	// Checando coluna relativa à última jogada:
    	for (int j = 0; j < board.length; j++) {
    		if (board[iLastPlay][j] != lastMark)
    			break; 
    		if (j == board.length-1) 
    			return lastMark;
    	}

    	// Checando linha relativa à última jogada:
    	for (int i = 0; i < board.length; i++) {
    		if (board[i][jLastPlay] != lastMark)
    			break;
    		if (i == board.length-1) 
    			return lastMark;
    	}

    	// Checando a diagonal, se a última jogada tiver sido nela:
    	if (iLastPlay == jLastPlay) {
    		for (int i = 0; i < board.length; i++) {
    			if (board[i][i] != lastMark)
    				break;
    			if (i == board.length-1) 
        			return lastMark;
    		}
    	}

        // Checando a outra diagonal...
    	for (int i = 0; i < board.length; i++) {
    		if (board[i][(board.length-1) - i] != lastMark)
    			break;
    		if (i == board.length-1)
    			return lastMark;
    	}
    	
    	// Se o algoritmo chegou até aqui E todas as jogadas foram
    	// feitas, houve empate; senão, o jogo ainda não acabou.
    	if (playCount == (board.length*board.length)) return DRAW;
    	else return NO_MARK;
	}
}
