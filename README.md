# Android-FLARE-Coston-Messenger
27/10/2021
With signs of life in the Flare camp I have for the last couple of weeks resumed work on this project.
If there is anyone out there who would like to give me feedback then I'm all ears. Yes it lacks polish
but the point is that it works and it's an exercise in learning both for me, and hopefully for anyone
who clones it and peruses the source code :)
I've ironed out a lot of bugs recently and completely changed how a lot of things work.               

Android 8 or above is supported. I may be able to get it to work on 6+ but don't hold your breath :)

Contributions most welcome!

15/05/2021
Testnet is up and running at https://testnet.xrpdevs.co.uk. Flare Networks testnet is still only private beta.
Have set the default RPC endpoint to point to my own testnet.

Fixed bugs in "new user" flow, was crashing if no wallets / PIN defined.
Added "create wallet" and associated code to create valid privkey, pubkey and address (verified working)
Added ability to send ERC20 tokens to other contacts.

23/04/2021
Development has been halted for several weeks now due to Flare's Coston being down.
I'm considering running a private testnet myself to enable myself (and possibly others) to continue working with Coston.

05/03/2021
I have noticed that people are cloning this project. If you like what you see and would like me to continue to develop this as
an open source project, please leave me some feedback, or start contributing.

[![Join the chat at https://gitter.im/xrpdevs/FlareNetMessenger](https://badges.gitter.im/xrpdevs/FlareNetMessenger.svg)](https://gitter.im/xrpdevs/FlareNetMessenger?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Our first attempt at building an app using smart contracts on the flare testnet. Using Android as host OS.

Using Web3j 
https://github.com/web3j/web3j

[![For information about how to use this app click here]](https://xrpdevs.co.uk/2021/02/15/updates-to-flarenetmessenger/)


To generate a wallet on the flare testnet to use with this app please see
https://github.com/flare-dev/coston

TODO: Add "Export to printer" to wallets activity for making hard copies of wallets.