<?php
/*
Plugin Name: Comment Contest
Plugin URI: http://www.nozzhy.com/un-plugin-pour-gerer-un-concours-par-commentaires-sur-votre-blog/
Description: If you create a contest on your website, you can draw all comments in a specific post
Author: Thomas "Zhykos" Cicognani
Version: 1.1.0.1
Author URI: http://www.nozzhy.com
*/

/**
 * Manage the comments' contest
 * @author Thomas "Zhykos" Cicognani
 * @see www.nozzhy.com
 */
class CommentContest {
	var $domain = '';
	var $version = '1.1.0.1'; // Current version
	var $option_ns = '';
	var $options = array ();
	var $localizationName = "commentContest";
	
	/**
	 * Add action to do (auto-generated method)
	 * @param $name The action's name
	 * @param $num ?
	 */
	function add_action($name, $num = 0) {
		$hook = $name;
		$fonction = $name;
		if (! $num) {
			$fonction .= $num;
		}
		add_action ( $hook, array (&$this, 'action_' . $name ) );
	}
	
	/**
	 * Create a new contest
	 */
	function CommentContest() {
		// Initialization
		if ($this->domain == '')
			$this->domain = get_class ( $this );
		
		if ($this->option_ns == '')
			$this->option_ns = get_class ( $this );
			
		// Get options
		$this->options = get_option ( $this->option_ns );
		
		// Launch the install?
		if (! isset ( $this->options ['install'] ) or ($this->options ['install'] != $this->version))
			$this->install ();
			
		// Load translation files
		$wp_ajax_edit_comments_locale = get_locale();
		$wp_ajax_edit_comments_mofile = WP_CONTENT_DIR . "/plugins/comment-contest/languages/" . $this->localizationName . "-". $wp_ajax_edit_comments_locale.".mo";
		load_textdomain($this->localizationName, $wp_ajax_edit_comments_mofile);
		
		// Manage actions
		foreach ( get_class_methods ( get_class ( $this ) ) as $methode ) {
			if (substr ( $methode, 0, 7 ) == 'action_') {
				$this->add_action ( substr ( $methode, 7 ) );
			}
		}
	
	}
	
	/**
	 * Things to do in the administration menu<br />
	 * Here we add "Comment Contest" in the plugin menu
	 */
	function action_admin_menu() {
		if (function_exists ( 'add_submenu_page' )) {
			add_submenu_page ( 'plugins.php', __ ( 'Comment Contest', $this->localizationName ), __ ( 'Comment Contest', $this->localizationName ), 3, basename ( __FILE__ ), array (&$this, 'AdminHelpPage' ) );
		}
	}
	
	/**
	 * Set an option
	 * @param $option The option to change
	 * @param $value The new value for the option
	 */
	function set($option, $value) {
		$this->options [$option] = $value;
	}
	
	/**
	 * Get the option's value
	 * @param $option The option we want to get
	 * @return The option's value
	 */
	function get($option) {
		if (isset ( $this->options [$option] )) {
			return $this->options [$option];
		} else {
			return false;
		}
	}
	
	/**
	 * Update the options
	 */
	function update_options() {
		return update_option ( $this->option_ns, $this->options );
	}
	
	//---------------------------------------------
	// Please edit this file from here
	//---------------------------------------------
	

	/**
	 * Method launched when we install the plugin
	 * @return unknown_type
	 */
	function install() {
		$this->set ( 'install', $this->version );
		$this->set ( 'page', 0 );
		$this->update_options ();
	}
	
	/**
	 * First step<br />
	 * Configure the contest's settings
	 */
	private function configure() {
		$configureContest = __("Configure the contest", $this->localizationName);
		$winnersNumber = __("Winners' number", $this->localizationName);
		$participationsNumber = __("Maximum participations' number per person", $this->localizationName);
		$allowedRanks = __("Allowed ranks to participate", $this->localizationName);
		$administrator = __("Administrator", $this->localizationName);
		$editor = __("Editor", $this->localizationName);
		$author = __("Author", $this->localizationName);
		$contributor = __("Contributor", $this->localizationName);
		$subscriber = __("Subscriber", $this->localizationName);
		$user = __("Normal user", $this->localizationName);
		$ok = __("Ok!", $this->localizationName);
		
		echo "<h1>Comment Contest - $configureContest</h1>
		<form action='plugins.php?page=comment-contest.php' method='post'>
		$winnersNumber: <input type='text' name='winners' value='2' /><br />
		$participationsNumber: <input type='text' name='number' value='1' /><br />
		<table>
			<tr style='vertical-align: top'>
				<td>$allowedRanks:</td>
				<td>
					<input type='checkbox' name='rank[]' value='10' /> $administrator<br />
					<input type='checkbox' name='rank[]' value='7' /> $editor<br />
					<input type='checkbox' name='rank[]' value='2' /> $author<br />
					<input type='checkbox' name='rank[]' value='1' /> $contributor<br />
					<input type='checkbox' name='rank[]' value='0' /> $subscriber<br />
					<input type='checkbox' name='rank[]' value='-1' checked='checked' /> $user
				</td>
			</tr>
		</table>
		<br /><input type='submit' name='features' value='$ok' /></form>";
	}
	
	/**
	 * Second step<br />
	 * Choose the post for the comment contest
	 * @param $currentPage The current page (post display)
	 * @param $ranks The ranks which are allowed to participate
	 * @param $numWinners The winners' number
	 * @param $numParticipation The participation's number for each person
	 */
	private function choosePost($currentPage, $ranks, $numWinners, $numParticipation) {
		global $wpdb;
		$maxArticles = 20;
		
		$choosePost = __("Choose a post", $this->localizationName);
		$more = __("More...", $this->localizationName);
		$noPost = __("No post found!", $this->localizationName);
		
		echo "<h1>Comment Contest - $choosePost</h1><form id='postForm' action='plugins.php?page=comment-contest.php' method='post'>";
		$query = "SELECT * FROM $wpdb->posts WHERE post_status = 'publish' AND post_type='post' ORDER BY post_date DESC";
		$posts = $wpdb->get_results ( $query );
		if ($posts) {
			$url = get_bloginfo ( 'url' );
			for($i = $currentPage; $i < count ( $posts ) && $i < $maxArticles + $currentPage; $i ++) {
				$post = $posts [$i];
				echo "<a href='#' onclick='getElementById(\"postnumber\").value=$post->ID; getElementById(\"postForm\").submit()'>$post->post_title</a><br />";
			}
			
			echo "<input type='hidden' name='rank' value='$ranks' />
			<input type='hidden' name='numWinners' value='$numWinners' />
			<input type='hidden' name='numParticipation' value='$numParticipation' />
			<input type='hidden' name='postnumber' id='postnumber' value='' /></form>";
			if (count ( $posts ) > $maxArticles) {
				echo "<br /><a href='$url/wp-admin/plugins.php?page=comment-contest.php&amp;pagepost=" . ($currentPage + $maxArticles) . "'>$more</a>";
			}
		} else {
			$this->error ( $noPost, array ("home") );
		}
	}
	
	/**
	 * Third step<br />
	 * Choose the comments
	 * @param $idPost The post's ID
	 * @param $ranks The ranks which are allowed to participate
	 * @param $numWinners The winners' number
	 * @param $numParticipation The participation's number for each person
	 */
	private function chooseComments($idPost, $ranks, $numWinners, $numParticipation) {
		global $wpdb;
		
		$chooseComments = __("Choose comments to include in the contest", $this->localizationName);
		$launchContest = __("Launch the contest", $this->localizationName);
		$noComment = __("No comment found!", $this->localizationName);
		
		echo "<h1>Comment Contest - $chooseComments</h1>";
		
		$filter = null;
		foreach ( explode ( ",", $ranks ) as $rank ) {
			if ($rank != - 1 && $rank != 0) {
				$queryTemp = "select user_login from $wpdb->usermeta, $wpdb->users where meta_key='wp_user_level' and  meta_value=$rank and user_id=ID";
				$resultQueryTemp = $wpdb->get_results ( $queryTemp );
				foreach ( $resultQueryTemp as $resultTemp ) {
					$filter [] = "comment_author = '$resultTemp->user_login'";
				}
			} elseif ($rank == 0) { // Subscriber
				$queryTemp = "select meta_value from $wpdb->usermeta where meta_key='nickname' and user_id not in(select distinct user_id from $wpdb->usermeta where meta_key='wp_user_level')";
				$resultQueryTemp = $wpdb->get_results ( $queryTemp );
				foreach ( $resultQueryTemp as $resultTemp ) {
					$filter [] = "comment_author = '$resultTemp->meta_value'";
				}
			} else { // User ($rank == -1)
				$queryTemp = "SELECT comment_author FROM $wpdb->comments WHERE comment_approved = \"1\" and comment_post_id='$idPost' and comment_author not in (select meta_value from $wpdb->usermeta where meta_key='nickname')";
				$resultQueryTemp = $wpdb->get_results ( $queryTemp );
				foreach ( $resultQueryTemp as $resultTemp ) {
					$filter [] = "comment_author = '$resultTemp->comment_author'";
				}
			}
		}
		
		$query = "SELECT * FROM $wpdb->comments WHERE comment_approved = \"1\" and comment_post_id='$idPost' and comment_author != (
			select user_login from $wpdb->users u, $wpdb->posts p where u.ID = post_author and p.ID = $idPost
			) and (" . implode ( " OR ", $filter ) . ") ORDER BY comment_author";
		$comments = $wpdb->get_results ( $query );
		
		if ($comments) {
			echo "<form action='plugins.php?page=comment-contest.php' method='post'>";
			$author = "";
			$count = 0;
			foreach ( $comments as $comment ) {
				$checked = " checked='checked'";
				$from = stripslashes ( $comment->comment_author );
				if ($author != $from) {
					$count = 0;
					$author = $from;
				} else {
					$count ++;
					if ($count >= $numParticipation) {
						$checked = "";
					}
				}
				$comment_content = stripslashes ( $comment->comment_content );
				$id = stripslashes ( $comment->comment_ID );
				
				echo "<input type='checkbox' name='comments[]' value='$id'$checked /> <strong>$from : </strong>" . substr ( strip_tags ( $comment_content ), 0, 100 ) . "<br /><br />";
			}
			echo "<br/><input type='hidden' name='post' value='$idPost' />
			<input type='hidden' name='rank' value='$ranks' />
			<input type='hidden' name='numWinners' value='$numWinners' />
			<input type='hidden' name='numParticipation' value='$numParticipation' />
			<input type='submit' value='$launchContest' /></form>";
		} else {
			$this->error ( $noComment, array ("home") );
		}
	}
	
	/**
	 * Last step<br />
	 * Display winners
	 * @param $comments <array[<integer>=><integer>]> The complete participants' list (created with all checkboxes)
	 * @param $numWinners The winners' number
	 * @param $numParticipation The participation's number for each person
	 */
	private function displayWinners($comments, $numWinners, $numParticipation) {
		global $wpdb;
		
		$winners = __("Winners", $this->localizationName);
		$commentWord = __("Comment", $this->localizationName);
		$say = __("says", $this->localizationName);
		
		$tab = null;
		foreach ( $comments as $comment => $value ) {
			$tab [] = $value;
		}
		
		shuffle ( $tab );
		echo "<h1>Comment Contest - $winners</h1>";
		$stop = false;
		$i = 1;
		$author = "";
		for($j = 0; $j < count ( $tab ) && ! $stop; $j ++) {
			$query = "SELECT * FROM $wpdb->comments WHERE comment_approved = '1' and comment_id='$tab[$j]'";
			$comment = $wpdb->get_results ( $query );
			$c = $comment [0];
			$from = $c->comment_author;
			
			if ($from != $author) {
				$i ++;
				$author = $from;
				echo "<strong>$commentWord</strong> $from <strong>$say</strong> $c->comment_content <br /><br />";
			}
			
			if ($i > $numWinners) {
				$stop = true;
			}
		}
	}
	
	/**
	 * Display an error message
	 * @param $message The error message
	 * @param $args The message's parameter.<br />$args[0] must be "post"
	 */
	private function error($message, $args) {
		$url = get_bloginfo ( 'url' );
		if ($args [0] == 'post') {
			$chooseComment = __("Choose comments", $this->localizationName);
			
			die ( "$message<br /><br />
			<form action='plugins.php?page=comment-contest.php' method='post'>
			<input type='hidden' name='postnumber' value='$args[1]' />
			<input type='hidden' name='rank' value='$args[2]' />
			<input type='hidden' name='numWinners' value='$args[3]' />
			<input type='hidden' name='numParticipation' value='$args[4]' />
			<input type='submit' value='$chooseComment' /></form>" );
		} elseif ($args [0] == 'home') {
			$back = __("Back", $this->localizationName);
			
			die ( "$message<br /><br /><a href='$url/wp-admin/plugins.php?page=comment-contest.php'>$back</a>" );
		}
	}
	
	/**
	 * The page to display in the administration menu
	 */
	function AdminHelpPage() {
		if (isset ( $_POST ['postnumber'] )) { // Step 3 : Choose comments
			$this->chooseComments ( $_POST ['postnumber'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'] );
		} elseif (isset ( $_POST ['post'] )) { // Step 4 : Display winners
			$comments = $_POST ['comments'];
			
			if ($comments == null || count ( $comments ) == 0) {
				$selectOneWinner = __("Please select one winner at least!", $this->localizationName);
				$this->error ( $selectOneWinner, array ("post", $_POST ['post'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'] ) );
			} elseif (count ( $comments ) <= $_POST ['numWinners']) {
				$selectMoreWinner = __("Please select more participants than winners!", $this->localizationName);
				$this->error ( $selectMoreWinner, array ("post", $_POST ['post'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'] ) );
			} else {
				$this->displayWinners ( $comments, $_POST ['numWinners'], $_POST ['numParticipation'] );
			}
		} elseif (isset ( $_POST ['features'] ) || isset ( $_GET ['pagepost'] )) { // Step 2 : Choose an article
			// Change the page if needed
			$page = (isset ( $_GET ['pagepost'] ) ? intval ( $_GET ['pagepost'] ) : 0);
			$page = ($page > 0 ? $page : 0);
			
			// Check list
			$numWinners = intval ( $_POST ['winners'] );
			$numParticipation = intval ( $_POST ['number'] );
			if (count ( $_POST ['rank'] ) == 0) {
				$selectOneRank = __("Please select one rank at least!", $this->localizationName);
				$this->error ( $selectOneRank, array ("home" ) );
			} elseif ($numWinners == null || $numWinners <= 0) {
				$winnerFormat = __("Wrong winners format!", $this->localizationName);
				$this->error ( $winnerFormat, array ("home" ) );
			} elseif ($numParticipation == null || $numParticipation <= 0) {
				$participationsFormat = __("Wrong participations format!", $this->localizationName);
				$this->error ( $participationsFormat, array ("home" ) );
			} else {
				$this->choosePost ( $page, implode ( ",", $_POST ['rank'] ), $numWinners, $numParticipation );
			}
		} else { // Step 1 : Configure the contest
			$this->configure ();
		}
	}
	
	/**
	 * Change the website's title
	 * @param $title The new title
	 */
	function action_wp_title($title) {
		return $title;
	}
	
//---------------------------------------------
// Stop edit from here
//---------------------------------------------


}

$inst_CommentContest = new CommentContest ( );

?>