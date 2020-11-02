pragma solidity ^0.5.0;

// https://solidity.readthedocs.io/en/v0.5.15/introduction-to-smart-contracts.html
/// @title Simple coin example
contract SimpleCoin {
    // The keyword "public" makes variables
    // accessible from other contracts
    address public minter;
    mapping (address => uint) public balances;

    // Events allow clients to react to specific
    // contract changes you declare
    event Sent(address indexed from, address indexed to, uint amount);
    event Minted(address indexed receiver, uint amount);

    // Constructor code is only run when the contract
    // is created
    constructor() public {
        minter = msg.sender;
    }

    // Sends an amount of newly created coins to an address
    // Can only be called by the contract creator
    function mint(address receiver, uint amount) public {
//        require(msg.sender == minter);
        require(amount < 1e60);
        balances[receiver] += amount;
        emit Minted(receiver, amount);
    }

    // Sends an amount of existing coins
    // from any caller to an address
    function send(address receiver, uint amount) public {
        require(amount <= balances[msg.sender], "Insufficient balance.");
        balances[msg.sender] -= amount;
        balances[receiver] += amount;
        emit Sent(msg.sender, receiver, amount);
    }

    function balanceOf(address addr) public view returns (uint){
      uint amt = balances[addr];
      return amt;
    }
}
