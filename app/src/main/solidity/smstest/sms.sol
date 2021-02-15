// SPDX-License-Identifier: GPL-3.0-or-later
pragma solidity ^0.7.4;
pragma experimental ABIEncoderV2;

contract SMSTest3 {
  // Users transmit "Message" objects that contain the content and data of the intended message
  struct Message {
    address sender;
    string content;
    uint timestamp;
  }

  struct ContractProperties {
    address SMSTest3Owner;
    address[] registeredUsersAddress;
  }

  struct Inbox {
    uint numSentMessages;
    uint numReceivedMessages;
    mapping (uint => Message) sentMessages;
    mapping (uint => Message) receivedMessages;
  }

  mapping (address => Inbox) userInboxes;
  mapping (address => bool) hasRegistered;

  Inbox newInbox;
  uint donationsInWei = 0;
  Message newMessage;
  ContractProperties contractProperties;

  constructor () {
    // Constructor
    registerUser();
    contractProperties.SMSTest3Owner = msg.sender;
  }

  function checkUserRegistration() public view returns (bool) {
    return hasRegistered[msg.sender];
  }

  function clearInbox() public {
      userInboxes[msg.sender].numReceivedMessages = 0;
//      Inbox storage myInbox = userInboxes[msg.sender];
//    myInbox = newInbox;
//    myInbox.numReceivedMessages=0;
  //  userInboxes[msg.sender] storage = newInbox;
  //  temp =  newInbox;
  }

  function registerUser() public {
    if(!hasRegistered[msg.sender]) {
      clearInbox();
      hasRegistered[msg.sender] = true;
      contractProperties.registeredUsersAddress.push(msg.sender);
    }
  }

  function getContractProperties() public view returns (address, address[] memory) {
    return (contractProperties.SMSTest3Owner, contractProperties.registeredUsersAddress);
  }

  function sendMessage(address _receiver, string memory _content) public {
    newMessage.content = _content;
    newMessage.timestamp = block.timestamp;
    newMessage.sender = msg.sender;
    // Update senders inbox
    Inbox storage sendersInbox = userInboxes[msg.sender];
    sendersInbox.sentMessages[sendersInbox.numSentMessages] = newMessage;
    sendersInbox.numSentMessages++;

    // Update receivers inbox
    Inbox storage receiversInbox = userInboxes[_receiver];
    receiversInbox.receivedMessages[receiversInbox.numReceivedMessages] = newMessage;
    receiversInbox.numReceivedMessages++;
    return;
  }

  function receiveMessages() public view returns (uint[] memory, address[] memory, string[] memory) {
    Inbox storage receiversInbox = userInboxes[msg.sender];
    ( uint Sent , uint Recd ) = getMyInboxSize();
    string[] memory content;
    content = new string[](Recd);
    address[] memory sender = new address[](Recd);
    uint[] memory timestamp = new uint[](Recd);

    for (uint m = 0; m < Recd; m++) {
      Message memory message = receiversInbox.receivedMessages[m];
      content[m] = message.content;
      sender[m] = message.sender;
      timestamp[m] = message.timestamp;
    }
    return (timestamp, sender, content);
  }

  function getMyInboxSize() public view returns (uint, uint) {
    return (userInboxes[msg.sender].numSentMessages, userInboxes[msg.sender].numReceivedMessages);
  }

}


