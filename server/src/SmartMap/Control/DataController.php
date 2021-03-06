<?php

namespace SmartMap\Control;

use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;

use SmartMap\DBInterface\UserRepositoryInterface;
use SmartMap\DBInterface\DatabaseException;

/**
 * 
 *
 * @author Pamoi
 *
 * @author SpicyCH (code reviewed - 03.11.2014) : good logic, but need unit testing!
 */
class DataController implements DataControllerInterface
{
    private $mRepo;
    
    function __construct(UserRepositoryInterface $repo)
    {
        $this->mRepo = $repo;
    }

    /**
     * Updates the user's position.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function updatePos(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        $longitude = RequestUtils::getPostParam($request, 'longitude');
        
        $latitude = RequestUtils::getPostParam($request, 'latitude');
        
        try
        {
            $user = $this->mRepo->getUser($userId);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in updatePos.', 2, $e);
        }
        
        try
        {
            $user->setLongitude($longitude);
            $user->setLatitude($latitude);
        }
        catch (\InvalidArgumentException $e)
        {
            throw new InvalidRequestException('Invalid coordinates.');
        }
        
        try
        {
            $this->mRepo->updateUser($user);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in updatePos.', 2, $e);
        }
        $response = array('status' => 'Ok', 'message' => 'Updated position !');
        
        return new JsonResponse($response);
    }

    /**
     * Gets the position of followed friends allowing it.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function listFriendsPos(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        try
        {
            $user = $this->mRepo->getUser($userId);

            $friendIds = $this->mRepo->getFriendsIds($userId, array('ALLOWED'), array('FOLLOWED'));
            
            $friends = $this->mRepo->getUsers($friendIds, array('VISIBLE'));
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in listFriendsPos.', 2, $e);
        }
        
        $list = array();

        // The user must be visible to see it's friends positions.
        if ($user->getVisibility() == 'VISIBLE')
        {
            foreach ($friends as $friend) {
                $list[] = array(
                    'id' => $friend->getId(),
                    'longitude' => $friend->getLongitude(),
                    'latitude' => $friend->getLatitude(),
                    'lastUpdate' => $friend->getLastUpdate(),
                    'isFriend' => 1
                );
            }
        }
        
        $response = array(
            'status' => 'Ok',
            'message' => 'Fetched friends positions !',
            'positions' => $list
        );
        
        return new JsonResponse($response);
    }

    /**
     * Get the user's friends ids.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function getFriendsIds(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        try
        {
            $friendsIds = $this->mRepo->getFriendsIds($userId);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in getFriendsIds.', 2, $e);
        }
        
        $response = array('status' => 'Ok', 'message' => 'Fetched friends !', 'friends' => $friendsIds);
        
        return new JsonResponse($response);
    }

    /**
     * Gets the information for the user whose id is given in user_id POST parameter.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function getUserInfo(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);

        $id = RequestUtils::getPostParam($request, 'user_id');
        
        try
        {
            $user = $this->mRepo->getUser($id);

            $friendsIds = $this->mRepo->getFriendsIds($userId);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in getUserInfo.', 2, $e);
        }

        $isFriend = 0;

        if (in_array($id, $friendsIds))
        {
            $isFriend = 1;
        }
        else if ($userId == $id)
        {
            $isFriend = 2;
        }

        // We only send public data
        $response = array(
            'status' => 'Ok',
            'message' => 'Fetched user info !',
            'id' => $user->getId(),
            'name' => $user->getName(),
            'isFriend' => $isFriend
        );
        
        return new JsonResponse($response);
    }

    /**
     * Sends an invitation to the user with id in post parameter.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     * @throws ServerFeedbackException
     */
    public function inviteFriend(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        $friendId = RequestUtils::getPostParam($request, 'friend_id');
        
        if ($userId == $friendId)
        {
            throw new ServerFeedbackException('You cannot invite yourself.');
        }
        
        try
        {
            // Check if the user is already friend or already invited or already inviting
            $friendsIds = $this->mRepo->getFriendsIds($userId,
                array('BLOCKED', 'ALLOWED', 'DISALLOWED'));
            
            $userInvitingIds = $this->mRepo->getInvitationIds($userId);
            $friendInvitingIds = $this->mRepo->getInvitationIds($friendId);
            
            if (!in_array($friendId, $friendsIds) AND !in_array($friendId, $userInvitingIds) AND
                !in_array($userId, $friendInvitingIds))
            {
                $this->mRepo->addInvitation($userId, $friendId);
            }
            else
            {
                throw new ServerFeedbackException('There is already a pending invitation ' .
                    'or you are already friends.');
            }
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in inviteFriend.', 2, $e);
        }
        
        $response = array('status' => 'Ok', 'message' => 'Invited friend !');
        
        return new JsonResponse($response);
    }

    /**
     * Gets a list of the pending invitations, a list of the friends that
     * accepted the user's invitation and a list of the ids of the friends
     * that removed the user.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function getInvitations(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        try
        {
            $inviterIds = $this->mRepo->getInvitationIds($userId);
            
            $inviters = $this->mRepo->getUsers($inviterIds);
            
            $acceptedInvitationIds = $this->mRepo->getAcceptedInvitations($userId);
            
            $newFriends = $this->mRepo->getUsers($acceptedInvitationIds);

            $removedIds = $this->mRepo->getRemovedFriends($userId);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in getInvitations.', 2, $e);
        }
        
        $invitersList = array();
        
        foreach ($inviters as $inviter)
        {
            $invitersList[] = array('id' => $inviter->getId(), 'name' => $inviter->getName(), 'isFriend' => 0);
        }
        
        $friendsList = array();
        
        foreach ($newFriends as $friend)
        {
            $friendsList[] = array(
                'id' => $friend->getId(),
                'name' => $friend->getName(),
                'longitude' => $friend->getLongitude(),
                'latitude' => $friend->getLatitude(),
                'lastUpdate' => $friend->getLastUpdate(),
                'isFriend' => 1
            );
        }
        
        $response = array(
            'status' => 'Ok',
            'message' => 'Fetched invitations !',
            'invitations' => $invitersList,
            'newFriends' => $friendsList,
            'removedFriends' => $removedIds
        );
        
        return new JsonResponse($response);
    }

    /**
     * Accepts an invitation.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function acceptInvitation(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        $friendId = RequestUtils::getPostParam($request, 'friend_id');
        
        try
        {
            // We check that the friend invited the user
            $invitersIds = $this->mRepo->getInvitationIds($userId);
            
            if (!in_array($friendId, $invitersIds))
            {
                throw new InvalidRequestException('Not invited by user with id ' . $friendId .' !');
            }
            
            $this->mRepo->removeInvitation($friendId, $userId);
        
            $this->mRepo->addFriendshipLink($userId, $friendId);
            
            $this->mRepo->addAcceptedInvitation($userId, $friendId);
        
            $user = $this->mRepo->getUser($friendId);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in acceptInvitation.', 2, $e);
        }
        
        $response = array(
            'status' => 'Ok',
            'message' => 'Accepted invitation !',
            'id' => $user->getId(),
            'name' => $user->getName(),
            'longitude' => $user->getLongitude(),
            'latitude' => $user->getLatitude(),
            'lastUpdate' => $user->getLastUpdate(),
            'isFriend' => 1
        );
        
        return new JsonResponse($response);
    }
    
    /**
     * Declines an invitation.
     * 
     * @param Request $request
     * @throws ControlLogicException
     * @return JsonResponse
     */
    public function declineInvitation(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        $friendId = RequestUtils::getPostParam($request, 'friend_id');
        
        try
        {
            $this->mRepo->removeInvitation($friendId, $userId);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in declineInvitation.', 2, $e);
        }
        
        $response = array('status' => 'Ok', 'message' => 'Declined invitation !');
        
        return new JsonResponse($response);
    }

    /**
     * Acknowledges an accepted invitation so it is no more sent in getInvitations.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function ackAcceptedInvitation(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        $friendId = RequestUtils::getPostParam($request, 'friend_id');
        
        try
        {
            $this->mRepo->removeAcceptedInvitation($userId, $friendId);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in ackAcceptedInvitation.', 2, $e);
        }
        
        $response = array('status' => 'Ok', 'message' => 'Acknowledged accepted invitation.');
        
        return new JsonResponse($response);
    }

    /**
     * Removes a friend.
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws ServerFeedbackException
     */
    public function removeFriend(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);
        
        $friendId = RequestUtils::getPostParam($request, 'friend_id');
        
        try
        {
            $removal = $this->mRepo->removeFriendshipLink($userId, $friendId);

            // We add a friend removal notification to update client's cache only if there was a friend to remove.
            if ($removal)
            {
                $this->mRepo->addRemovedFriend($userId, $friendId);
            }
            else
            {
                throw new ServerFeedbackException('You are not friend with this user.');
            }
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in removeFriend.', 2, $e);
        }

        $response = array('status' => 'Ok', 'message' => 'Removed friend !');
        
        return new JsonResponse($response);
    }

    /**
     * Acknowledges a removed friend so it is no longer sent in getInvitations
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function ackRemovedFriend(Request $request)
    {
        $userId = RequestUtils::getIdFromRequest($request);

        $friendId = RequestUtils::getPostParam($request, 'friend_id');

        try
        {
            $this->mRepo->removeRemovedFriend($userId, $friendId);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in ackRemovedFriend.', 2, $e);
        }

        $response = array('status' => 'Ok', 'message' => 'Acknowledged removed friend.');

        return new JsonResponse($response);
    }

    /**
     * Gets a list of user names and ids where name begins with post parameter
     * search_text (ignores case).
     *
     * @param Request $request
     * @return JsonResponse
     * @throws ControlLogicException
     * @throws InvalidRequestException
     */
    public function findUsers(Request $request)
    {
        // We check that we are authenticated.
        $id = RequestUtils::getIdFromRequest($request);
        
        $partialName = RequestUtils::getPostParam($request, 'search_text');
        
        try
        {
            // We do not return friends
            $nonDisplayedUsers = $this->mRepo->getFriendsIds($id);

            $nonDisplayedUsers[] = $id; // We do not want to show the user in the search results.

            // We do not return invited users
            $nonDisplayedUsers = array_merge($nonDisplayedUsers, $this->mRepo->getInvitedIds($id));

            // We do not want to return inviting users
            $nonDisplayedUsers = array_merge($nonDisplayedUsers, $this->mRepo->getInvitationIds($id));
            
            $users = $this->mRepo->findUsersByPartialName($partialName, $nonDisplayedUsers);
        }
        catch (DatabaseException $e)
        {
            throw new ControlLogicException('Error in findUsers.', 2, $e);
        }
        
        $data = array();
        foreach ($users as $user)
        {
            $data[] = array('id' => $user->getId(), 'name' => $user->getName());
        }
        
        $response = array('status' => 'Ok', 'message' => 'Fetched users !', 'list' => $data);
        
        return new JsonResponse($response);
    }
}
