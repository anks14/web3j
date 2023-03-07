// SPDX-License-Identifier: MIT
pragma solidity >=0.7.0;

contract Sum {

    function taker(uint a, uint b) public pure returns (uint){
        uint sum = a + b;
        return sum;
    }

}