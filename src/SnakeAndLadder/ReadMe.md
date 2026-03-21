# Snake and Ladder — LLD Interview Ready Sheet
> Target: Tier-1 (Google, Amazon, Flipkart) | Pattern: Strategy + State | Difficulty: Easy-Medium

---

## 1. Clarifying Questions (Ask These First)

- How many players? Fixed 2 or variable?
- Standard 100-cell board or configurable size?
- Single dice or two dice?
- Any special rules — e.g., exact roll needed to win, or ≥100 counts as win?
- Should snakes/ladders be configurable or hardcoded?

---

## 2. Core Entities

| Class | Key Fields | Responsibility |
|-------|-----------|---------------|
| `Board` | size, Map\<Integer, Integer\> teleportMap | Holds board state — O(1) snake/ladder lookup |
| `Snake` | start, end (start > end) | Represents a snake — used to populate teleportMap |
| `Ladder` | start, end (end > start) | Represents a ladder — used to populate teleportMap |
| `Dice` | sides | Rolls random number 1-6 |
| `Player` | playerId, name, currentPosition | Tracks player position |
| `Game` | Board, List\<Player\>, currentPlayerIndex, GameStatus | Holds game state |
| `GameController` | Game | Orchestrates turns, checks win condition |

### Enums
```java
enum GameStatus { IN_PROGRESS, FINISHED }
```

---

## 3. Key Design Insight — 1D Board + TeleportMap
> Most common mistake — treating board as 2D grid.

Snake & Ladder is a **1D board of 100 cells**, not a 2D grid.

```java
// WRONG
int[][] board = new int[100][100];  // 2D grid — incorrect

// RIGHT — 1D with teleport map
Map<Integer, Integer> teleportMap = new HashMap<>();
teleportMap.put(62, 10);  // snake:  62 → 10  (start > end)
teleportMap.put(17, 54);  // ladder: 17 → 54  (end > start)
// cells not in map = normal cells
```

**Why Map over List:** O(1) lookup vs O(n) search through snake/ladder lists on every move.

---

## 4. Board Setup

```java
public class Board {
    private final int size;
    private final Map<Integer, Integer> teleportMap;

    public Board(int size, List<Snake> snakes, List<Ladder> ladders) {
        this.size        = size;
        this.teleportMap = new HashMap<>();

        // Populate from snakes
        for (Snake snake : snakes) {
            if (snake.getStart() <= snake.getEnd())
                throw new IllegalArgumentException("Snake start must be > end");
            teleportMap.put(snake.getStart(), snake.getEnd());
        }

        // Populate from ladders
        for (Ladder ladder : ladders) {
            if (ladder.getEnd() <= ladder.getStart())
                throw new IllegalArgumentException("Ladder end must be > start");
            teleportMap.put(ladder.getStart(), ladder.getEnd());
        }
    }

    // Returns final position after applying snake/ladder
    public int getFinalPosition(int cell) {
        return teleportMap.getOrDefault(cell, cell);
    }

    public int getSize() { return size; }
}
```

---

## 5. Core Entities — Full Code

```java
public class Snake {
    int start, end;
    Snake(int start, int end) {
        if (start <= end) throw new IllegalArgumentException("Snake: start must be > end");
        this.start = start; this.end = end;
    }
}

public class Ladder {
    int start, end;
    Ladder(int start, int end) {
        if (end <= start) throw new IllegalArgumentException("Ladder: end must be > start");
        this.start = start; this.end = end;
    }
}

public class Dice {
    private final int sides;
    private final Random random = new Random();
    Dice(int sides) { this.sides = sides; }
    public int roll() { return random.nextInt(sides) + 1; }
}

public class Player {
    String playerId;
    String name;
    int currentPosition;  // starts at 0

    Player(String playerId, String name) {
        this.playerId        = playerId;
        this.name            = name;
        this.currentPosition = 0;
    }
}

public class Game {
    Board board;
    List<Player> players;
    int currentPlayerIndex;
    GameStatus status;
    Player winner;

    Game(Board board, List<Player> players) {
        this.board                = board;
        this.players              = players;
        this.currentPlayerIndex   = 0;
        this.status               = GameStatus.IN_PROGRESS;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
}
```

---

## 6. Core Algorithm — playTurn()

```java
public class GameController {
    private final Game game;
    private final Dice dice;

    public GameController(Game game, Dice dice) {
        this.game = game;
        this.dice = dice;
    }

    public void playTurn() {
        if (game.status == GameStatus.FINISHED) {
            System.out.println("Game already finished. Winner: " + game.winner.getName());
            return;
        }

        Player player = game.getCurrentPlayer();
        int roll = dice.roll();
        System.out.println(player.getName() + " rolled: " + roll);

        int newPosition = player.currentPosition + roll;

        // Can't go beyond board size
        if (newPosition > game.board.getSize()) {
            System.out.println("Roll too high — stay at " + player.currentPosition);
            game.nextTurn();
            return;
        }

        // Apply snake or ladder
        int finalPosition = game.board.getFinalPosition(newPosition);

        if (finalPosition < newPosition)
            System.out.println("Snake! " + newPosition + " → " + finalPosition);
        else if (finalPosition > newPosition)
            System.out.println("Ladder! " + newPosition + " → " + finalPosition);

        player.currentPosition = finalPosition;
        System.out.println(player.getName() + " is now at: " + finalPosition);

        // Check win condition
        if (finalPosition == game.board.getSize()) {
            game.status = GameStatus.FINISHED;
            game.winner = player;
            System.out.println("🎉 " + player.getName() + " wins!");
            return;
        }

        game.nextTurn();
    }

    public void startGame() {
        System.out.println("Game started!");
        while (game.status == GameStatus.IN_PROGRESS) {
            playTurn();
        }
    }
}
```

---

## 7. Demo — How It All Wires Together

```java
public class Main {
    public static void main(String[] args) {
        List<Snake> snakes = List.of(
            new Snake(62, 10),
            new Snake(95, 75),
            new Snake(99, 60)
        );
        List<Ladder> ladders = List.of(
            new Ladder(17, 54),
            new Ladder(35, 80),
            new Ladder(6, 25)
        );

        Board board = new Board(100, snakes, ladders);
        List<Player> players = List.of(
            new Player("P1", "Mohit"),
            new Player("P2", "Raj")
        );

        Game game = new Game(board, players);
        Dice dice = new Dice(6);
        GameController controller = new GameController(game, dice);
        controller.startGame();
    }
}
```

---

## 8. Curveballs + Answers

| Curveball | Answer |
|-----------|--------|
| Configurable board size | `Board(int size, ...)` — already parameterized |
| Two dice instead of one | `Dice` takes `sides` — add `count` field, roll twice and sum |
| Exact roll needed to win | Add check: `if (newPosition > boardSize) stay` — already handled |
| Player gets extra turn on rolling 6 | In `playTurn()` — skip `nextTurn()` if roll == 6 |
| Multiplayer (4 players) | `List<Player>` + `currentPlayerIndex % players.size()` — already supports it |
| Snake and ladder on same cell | `teleportMap` handles last-write-wins — document this as a constraint |

---

## 9. Mistakes to Avoid in Interview

| Mistake | Why It's Bad |
|---------|-------------|
| 2D board `int[100][100]` | Snake & Ladder is 1D — 2D shows fundamental misunderstanding |
| `List<Snake>` + `List<Ladder>` for lookup | O(n) search every move — use `Map<Integer, Integer>` for O(1) |
| No strict validation on Snake/Ladder | Snake with end > start is a ladder — silent bug |
| Win check missing | Player can overshoot 100 and never win |
| No `GameStatus` | Can't stop game loop cleanly after win |
| Game state inside Controller | Controller should orchestrate, `Game` should hold state — SRP |

---

## 10. One-Line Pattern Justifications
> Say these out loud in the interview.

- **TeleportMap over two Lists** — *"O(1) lookup on every move — List search is O(n) which is wasteful for a hot path."*
- **Board encapsulates teleport logic** — *"Controller shouldn't know about snakes and ladders — getFinalPosition() hides that complexity."*
- **Game holds state, Controller orchestrates** — *"SRP — Game owns what the state is, Controller owns what to do with it."*
- **Strict Snake/Ladder validation** — *"Silent bugs from misconfigured snakes are hard to debug — fail fast at construction time."*