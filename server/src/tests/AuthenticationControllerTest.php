<?php

use Silex\Application;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use SmartMap\DBInterface\User;
use SmartMap\DBInterface\UserRepository;
use SmartMap\Control\AuthenticationController;

use Facebook\FacebookSession;
use Facebook\FacebookRedirectLoginHelper;
use Facebook\FacebookRequest;
use Facebook\FacebookResponse;
use Facebook\FacebookSDKException;
use Facebook\FacebookRequestException;
use Facebook\FacebookAuthorizationException;
use Facebook\GraphObject;

use Doctrine\Tests\Mocks\ConnectionMock;

/**
 * The mock fb account is robin.genolet@epfl.ch hackerinside
 * name: Swag Sweng
 * ID: 
 * @author SpicyCH
 *
 */
class AuthenticationControllerTest extends PHPUnit_Framework_TestCase {
    public $test;
    
    public function setUp() {
        $this->test = 1;
        
    }
    
    public function testOne() {
        $one = 1;
        $this->assertTrue($one === 1);
    }
    
    public function testBadParametersLeadToError() {
        $request = Request::create(
            '/hello-world',
            'POST',
            array('name' => 'Robich')
        );
        
        $conn = null; // TODO find a way to instanciate the Connection
        $conn = Doctrine_Manager::connection('mysql://smartmap:salut123@localhost/test', 'connection 1');
        $userRepo = new UserRepository($conn);
        $authContr = new AuthenticationController($userRepo);
        
        $serverResponse = json_decode($authContr::authenticate($request));
        echo $serverResponse;
    }
}
    

?>