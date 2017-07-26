/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var isConnectFrame = function(json) {
	var j = JSON.parse(json);         
    //print('Hi there from Javascript, ' + j.vid);
    //return "greetings from javascript 2";
	return  j.vid !== undefined;
};

var getVid = function(json) {
	var j = JSON.parse(json);         
    //print('Hi there from Javascript, ' + j.vid);
	return  j.vid;
};