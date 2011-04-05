package CSE4705_final.Client;

public interface ClientInterface {

    void initialize();

    void opponentMove(ClientMove move);

    ClientMove getMove(int timer);
}
