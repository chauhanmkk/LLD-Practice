# Chess — LLD Interview Ready Sheet
> Target: Tier-1 (Google, Atlassian, Amazon) | Pattern: Strategy + State | Difficulty: Medium-Hard

---

## 1. Clarifying Questions (Ask These First)

- Standard chess rules or simplified version?
- Do we need full checkmate detection or just check detection?
- Two players only or support for AI opponent?
- Do we need move history / undo support?
- Timer per move (tournament mode)?

---

## 2. Core Entities

| Class | Key Fields | Responsibility |
|-------|-----------|---------------|
| `Board` | Cell\[8\]\[8\] grid | Holds all cells, initializes pieces |
| `Cell` | row, col, Piece | One square on the board |
| `Piece` | color, PieceType, MoveStrategy | Abstract — King/Queen/Rook etc extend it |
| `MoveStrategy` | interface | Move validation per piece type |
| `Player` | playerId, color (WHITE/BLACK) | Person making moves |
| `Game` | Board, List\<Player\>, currentPlayer, GameStatus | Holds game state |
| `GameController` | Game | Orchestrates turns, validates moves, checks win |
| `Move` | fromCell, toCell, Piece | Represents one move — useful for history |

### Enums
```java
enum Color      { WHITE, BLACK }
enum PieceType  { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
enum GameStatus { IN_PROGRESS, CHECK, CHECKMATE, STALEMATE, FINISHED }
```

---

## 3. How Chess Differs from Snake & Ladder

| Aspect | Snake & Ladder | Chess |
|--------|---------------|-------|
| Board | 1D teleport map | 2D 8×8 grid with Cell objects |
| Pieces | None — just position | 6 piece types with unique move rules |
| Core pattern | Simple OOP | Strategy per piece for move validation |
| Win condition | Reach cell 100 | King in checkmate |
| Key complexity | TeleportMap lookup | Move validation + check detection |

**Same structure reused from Snake & Ladder:**
- `Game` holding state
- `GameController` orchestrating turns
- `Player` with turn tracking
- `GameStatus` enum
- Win condition check after every move

---

## 4. Key Design Insight — Strategy Per Piece
> This is the #1 thing interviewers check for Chess.

Each piece has **unique movement rules** — without Strategy pattern you get:
```java
// BAD — giant if-else in one place
void validateMove(Piece piece, Cell from, Cell to) {
    if (piece.type == ROOK) { ... }
    else if (piece.type == BISHOP) { ... }
    else if (piece.type == KNIGHT) { ... }
    // grows forever — OCP violation
}

// GOOD — each piece owns its move logic
interface MoveStrategy {
    boolean isValidMove(Cell from, Cell to, Board board);
}
class RookMoveStrategy   implements MoveStrategy { ... }
class BishopMoveStrategy implements MoveStrategy { ... }
class KnightMoveStrategy implements MoveStrategy { ... }
```

---

## 5. Piece Hierarchy + Strategy

```java
// Abstract Piece
public abstract class Piece {
    Color color;
    PieceType type;
    MoveStrategy moveStrategy;

    Piece(Color color, PieceType type, MoveStrategy moveStrategy) {
        this.color        = color;
        this.type         = type;
        this.moveStrategy = moveStrategy;
    }

    public boolean canMove(Cell from, Cell to, Board board) {
        return moveStrategy.isValidMove(from, to, board);
    }
}

// Concrete pieces
public class Rook   extends Piece { Rook(Color c)   { super(c, PieceType.ROOK,   new RookMoveStrategy()); } }
public class Bishop extends Piece { Bishop(Color c) { super(c, PieceType.BISHOP, new BishopMoveStrategy()); } }
public class Knight extends Piece { Knight(Color c) { super(c, PieceType.KNIGHT, new KnightMoveStrategy()); } }
public class King   extends Piece { King(Color c)   { super(c, PieceType.KING,   new KingMoveStrategy()); } }
public class Queen  extends Piece { Queen(Color c)  { super(c, PieceType.QUEEN,  new QueenMoveStrategy()); } }
public class Pawn   extends Piece { Pawn(Color c)   { super(c, PieceType.PAWN,   new PawnMoveStrategy()); } }
```

---

## 6. Move Strategy Implementations

```java
public interface MoveStrategy {
    boolean isValidMove(Cell from, Cell to, Board board);
}

// Rook — moves horizontally or vertically, no pieces in path
public class RookMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(Cell from, Cell to, Board board) {
        // Must stay in same row OR same column
        if (from.getRow() != to.getRow() && from.getCol() != to.getCol()) return false;
        // Path must be clear
        return isPathClear(from, to, board);
    }

    private boolean isPathClear(Cell from, Cell to, Board board) {
        int rowDir = Integer.compare(to.getRow(), from.getRow());
        int colDir = Integer.compare(to.getCol(), from.getCol());
        int r = from.getRow() + rowDir;
        int c = from.getCol() + colDir;
        while (r != to.getRow() || c != to.getCol()) {
            if (board.getCell(r, c).getPiece() != null) return false;
            r += rowDir; c += colDir;
        }
        return true;
    }
}

// Bishop — moves diagonally, no pieces in path
public class BishopMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(Cell from, Cell to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());
        if (rowDiff != colDiff) return false; // must be diagonal
        return isPathClear(from, to, board);
    }

    private boolean isPathClear(Cell from, Cell to, Board board) {
        int rowDir = Integer.compare(to.getRow(), from.getRow());
        int colDir = Integer.compare(to.getCol(), from.getCol());
        int r = from.getRow() + rowDir;
        int c = from.getCol() + colDir;
        while (r != to.getRow() || c != to.getCol()) {
            if (board.getCell(r, c).getPiece() != null) return false;
            r += rowDir; c += colDir;
        }
        return true;
    }
}

// Knight — L-shape move, CAN jump over pieces
public class KnightMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(Cell from, Cell to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
        // No path check needed — Knight jumps
    }
}

// King — one step any direction
public class KingMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(Cell from, Cell to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());
        return rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0);
    }
}

// Queen — Rook + Bishop combined
public class QueenMoveStrategy implements MoveStrategy {
    private final RookMoveStrategy rook     = new RookMoveStrategy();
    private final BishopMoveStrategy bishop = new BishopMoveStrategy();

    @Override
    public boolean isValidMove(Cell from, Cell to, Board board) {
        return rook.isValidMove(from, to, board) || bishop.isValidMove(from, to, board);
    }
}
```

---

## 7. Board + Cell

```java
public class Cell {
    int row, col;
    Piece piece;  // null if empty

    Cell(int row, int col) { this.row = row; this.col = col; }
    public Piece getPiece()         { return piece; }
    public void setPiece(Piece p)   { this.piece = p; }
    public int getRow()             { return row; }
    public int getCol()             { return col; }
}

public class Board {
    Cell[][] grid = new Cell[8][8];

    public Board() {
        // Initialize cells
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                grid[r][c] = new Cell(r, c);
        initializePieces();
    }

    private void initializePieces() {
        // White pieces — row 0, 1
        grid[0][0].setPiece(new Rook(Color.WHITE));
        grid[0][1].setPiece(new Knight(Color.WHITE));
        grid[0][2].setPiece(new Bishop(Color.WHITE));
        grid[0][3].setPiece(new Queen(Color.WHITE));
        grid[0][4].setPiece(new King(Color.WHITE));
        for (int c = 0; c < 8; c++) grid[1][c].setPiece(new Pawn(Color.WHITE));

        // Black pieces — row 7, 6
        grid[7][0].setPiece(new Rook(Color.BLACK));
        grid[7][1].setPiece(new Knight(Color.BLACK));
        grid[7][2].setPiece(new Bishop(Color.BLACK));
        grid[7][3].setPiece(new Queen(Color.BLACK));
        grid[7][4].setPiece(new King(Color.BLACK));
        for (int c = 0; c < 8; c++) grid[6][c].setPiece(new Pawn(Color.BLACK));
    }

    public Cell getCell(int row, int col) { return grid[row][col]; }
}
```

---

## 8. Core Algorithm — makeMove()

```java
public class GameController {
    private final Game game;

    public GameController(Game game) { this.game = game; }

    public void makeMove(Player player, Cell from, Cell to) {
        if (game.getStatus() == GameStatus.FINISHED) {
            System.out.println("Game already over");
            return;
        }

        // 1. Validate it's this player's turn
        if (!game.getCurrentPlayer().equals(player))
            throw new RuntimeException("Not your turn");

        Piece piece = from.getPiece();

        // 2. Validate piece exists and belongs to player
        if (piece == null)
            throw new RuntimeException("No piece at source cell");
        if (piece.getColor() != player.getColor())
            throw new RuntimeException("Cannot move opponent's piece");

        // 3. Validate destination — can't capture own piece
        Piece target = to.getPiece();
        if (target != null && target.getColor() == player.getColor())
            throw new RuntimeException("Cannot capture your own piece");

        // 4. Validate move via Strategy
        if (!piece.canMove(from, to, game.getBoard()))
            throw new RuntimeException("Invalid move for " + piece.getType());

        // 5. Execute move
        to.setPiece(piece);
        from.setPiece(null);

        // 6. Check win condition — opponent's King captured
        if (target != null && target.getType() == PieceType.KING) {
            game.setStatus(GameStatus.FINISHED);
            game.setWinner(player);
            System.out.println(player.getName() + " wins!");
            return;
        }

        // 7. Next player's turn
        game.nextTurn();
    }
}
```

---

## 9. Curveballs + Answers

| Curveball | Answer |
|-----------|--------|
| Add checkmate detection | After every move, check if opponent's King has zero valid moves |
| Move history / undo | `Stack<Move>` — push on every move, pop to undo |
| Pawn promotion | In `PawnMoveStrategy` — if pawn reaches row 7/0, replace with Queen |
| Castling (King + Rook) | Special case in `KingMoveStrategy` — check neither has moved |
| AI opponent | Strategy pattern on `Player` — `HumanPlayer` vs `AIPlayer` with `MoveSelectionStrategy` |
| Multiplayer online | Observer pattern — broadcast board state after every move |

---

## 10. Mistakes to Avoid in Interview

| Mistake | Why It's Bad |
|---------|-------------|
| Move validation in one giant if-else | OCP violation — adding new piece requires modifying existing code |
| No path clear check for Rook/Bishop/Queen | Pieces can illegally jump over others |
| Knight path check | Knight jumps — no path validation needed, just L-shape check |
| Capturing own piece allowed | Missing color check on destination cell |
| Queen reimplementing Rook + Bishop logic | Compose existing strategies — `QueenMoveStrategy` delegates to both |
| Game state inside Controller | Controller orchestrates, `Game` holds state — SRP |

---

## 11. One-Line Pattern Justifications
> Say these out loud in the interview.

- **Strategy per piece** — *"Each piece has unique move rules — Strategy lets each piece own its logic without any if-else in the controller."*
- **Queen composes Rook + Bishop** — *"Queen moves like Rook or Bishop — composition over duplication."*
- **Cell holds Piece reference** — *"Board is a 2D grid of Cells, not pieces — Cell is the bridge entity between position and piece."*
- **Game holds state, Controller orchestrates** — *"SRP — Game owns what the state is, Controller owns what to do with it."*
- **Knight skips path check** — *"Knight jumps over pieces by design — path validation would be wrong here."*