package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;

public abstract class ChessPiece extends Piece {

	private Color color;

	public ChessPiece(Board board, Color color) {
		super(board);
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
	
	public ChessPosition getChessPosition(){
		//este position é herdado da protected Position position(na classe Piece), e temos de o converter para ChessPosition
		return ChessPosition.fromPosition(position);
	}
	
	//Esta operação vai ser reaporveitada em todas as peças, por isso fica nesta classe.
		//relembrar, protected é acessivel pelo pacote e subclasses
		protected boolean isThereOpponentPiece(Position position){
			ChessPiece p =(ChessPiece)getBoard().piece(position);
			return p != null && p.getColor() != color;
		}

}
