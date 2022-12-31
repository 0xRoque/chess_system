package boardgame;


public abstract class Piece {

	protected Position position;
	private Board board;

	public Piece(Board board) {
		// não tem a peça criada pq a posição dela inicialmente é nula
		this.board = board;
		position = null;
	}

	protected Board getBoard() {
		return board;
	}
	//Definir movimentos possiveis
	public abstract boolean[][] possibleMoves();
	
	public boolean possibleMove(Position position) {
		return possibleMoves()[position.getRow()][position.getColumn()];
	}
	
	public boolean isThereAnyPossibleMove() {
		boolean [][] mat = possibleMoves();
		//varrer a matriz para verficiar se existe pelo menos uma posição possivel 
		for(int i =0; i<mat.length;i++) {
			for (int j =0; j<mat.length;j++) {
				//existe movimento
				if(mat[i][j]) {
					return true;
				}
			}
		}
		//nao existe movimento possivel
		return false;
	}
}
