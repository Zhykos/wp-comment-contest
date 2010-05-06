<?php
/*
Plugin Name: Comment Contest
Plugin URI: http://www.nozzhy.com/plugins/comment-contest-description/
Description: If you create a contest on your website, you can draw all comments in a specific post
Author: Thomas "Zhykos" Cicognani
Version: 1.40.1
Author URI: http://www.nozzhy.com
*/

/*  Copyright 2009  Comment Contest plugin for Wordpress by Thomas "Zhykos" Cicognani  (email : zhykos@nozzhy.com)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

/**
 * Manage the comments' contest
 * @author Thomas "Zhykos" Cicognani
 * @see www.nozzhy.com
 */
class CommentContest {
	/*private*/var $domain = '';
	/*private*/var $version = '1.40.1'; // Current version
	/*private*/var $option_ns = '';
	/*private*/var $options = array ();
	/*private*/var $localizationName = "commentContest";
	/*private*/var $pluginDir = '';
	
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
			
		$this->pluginDir = WP_CONTENT_URL . '/plugins/comment-contest';
			
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
	 * @param $errorCode Error code (it's the message to display). <i>Null</i> means thers's no error
	 * @param $previousContestType Contest Type
	 * @param $numWinners Winners' number
	 * @param $numParticipation Maximum participation's number
	 * @param $numPrizes Prizes' number
	 * @param $ranks Allowed ranks
	 * @param $email Email to send (<i>null</i> means no email to send)
	 * @param $mailsubject [string] Email subject
	 */
	function step1_configure($errorCode = null, $previousContestType = null, $numWinners = null, $numParticipation = null, $numPrizes = null, $ranks = null, $email = null, $mailsubject = null) {
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
		$sendmail = __("Automaticaly send a mail to winners", $this->localizationName);
		$mailcontent = __("Email's content", $this->localizationName);
		$contestType = __("Contest type", $this->localizationName);
		$ok = __("Ok!", $this->localizationName);
		$normalContest = __("Normal contest (random winners)", $this->localizationName);
		$speedContest = __("Speed contest (first comments win)", $this->localizationName);
		$prizeContest = __("Number of different prizes", $this->localizationName);
		$mailSubjectTranslation = __("Email subject", $this->localizationName);
		$sTip1Translation = __("Tip 1", $this->localizationName);
		$sTip2Translation = __("Tip 2", $this->localizationName);
		$sTip1ContentTranslation = __("Put a \"More\" tag and write the message for the loosers after!", $this->localizationName);
		$sTip2ContentTranslation = __("Write \"%prize%\" (without quotes) and I will automatically replace it with the real name of the price!", $this->localizationName);
		
		// V1.4 - ADD : Include Wordpress Editor to write the email
		// BEGIN V1.4
		add_filter('admin_head','zd_multilang_tinymce');
		wp_admin_css('thickbox');
		wp_print_scripts('jquery-ui-core');
		wp_print_scripts('jquery-ui-tabs');
		wp_print_scripts('post');
		wp_print_scripts('editor');
		add_thickbox();
		wp_print_scripts('media-upload');
		if (function_exists('wp_tiny_mce')) {
			wp_tiny_mce();
		}
		// END V1.4

		echo "<h1>Comment Contest - $configureContest</h1>";
		
		if($errorCode != null) {
			echo "<div id='message' class='error'><p>$errorCode</p></div>";
		}
		
		$normalType = " checked='checked'";
		$speedType = "";
		
		if($previousContestType != null) {
			if($previousContestType == "speed") {
				$normalType = "";
				$speedType = " checked='checked'";
			}
		}
		
		$winnersValue = ($numWinners == null ? 2 : $numWinners);
		$participationsValue = ($numParticipation == null ? 1 : $numParticipation);
		$prizeValue = ($numPrizes == null ? 1 : $numPrizes);
		$userChecked = " checked='checked'";
		
		if($ranks != null) {
			if(in_array(10, $ranks)) {
				$adminChecked = " checked='checked'";
			} else {
				$adminChecked = "";
			}
			
			if(in_array(7, $ranks)) {
				$editorChecked = " checked='checked'";
			} else {
				$editorChecked = "";
			}
			
			if(in_array(2, $ranks)) {
				$authorChecked = " checked='checked'";
			} else {
				$authorChecked = "";
			}
			
			if(in_array(1, $ranks)) {
				$contributorChecked = " checked='checked'";
			} else {
				$contributorChecked = "";
			}
			
			if(in_array(0, $ranks)) {
				$subscriberChecked = " checked='checked'";
			} else {
				$subscriberChecked = "";
			}
			
			if(!in_array(-1, $ranks)) {
				$userChecked = "";
			}
		}
		
		if($email != null) {
			$emailChecked = " checked='checked'";
			$mailcontent = stripslashes(base64_decode($email));
		} else {
			$emailChecked = "";
		}
		
		echo "<form action='plugins.php?page=comment-contest.php' method='post'>
			<table style='margin-left: -2px'>
			<tr style='vertical-align: top'>
				<td>$contestType:</td>
				<td>
					<input type='radio' name='contestType' value='normal'$normalType /> $normalContest<br />
					<input type='radio' name='contestType' value='speed'$speedType /> $speedContest
				</td> 
			</tr>
		</table><br />
		$winnersNumber: <input type='text' name='numWinners' value='$winnersValue' /><br />
		$participationsNumber: <input type='text' name='numParticipation' value='$participationsValue' /><br />
		$prizeContest: <input type='text' name='numPrizes' value='$prizeValue' /><br />
		<table style='margin-left: -2px'>
			<tr style='vertical-align: top'>
				<td>$allowedRanks:</td>
				<td>
					<input type='checkbox' name='rank[]' value='10'$adminChecked /> $administrator<br />
					<input type='checkbox' name='rank[]' value='7'$editorChecked /> $editor<br />
					<input type='checkbox' name='rank[]' value='2'$authorChecked /> $author<br />
					<input type='checkbox' name='rank[]' value='1'$contributorChecked /> $contributor<br />
					<input type='checkbox' name='rank[]' value='0'$subscriberChecked /> $subscriber<br />
					<input type='checkbox' name='rank[]' value='-1'$userChecked /> $user
				</td>
			</tr>
		</table>
		<br /><hr />
		<input type='checkbox' name='sendmail'$emailChecked /> $sendmail<br /><br />
		<div style=\"width: 800px;\">
		$mailSubjectTranslation : <input type=\"text\" name=\"mailsubject\" value=\"$mailsubject\" size=\"80\" maxlength=\"80\" /><br /><br />";
		
		the_editor($mailcontent, "mailcontent", "mailcontent", false); // V1.4 : ADD - Display the editor. Also update HTML Content above and under
		
		echo "</div><br />
		<b>$sTip1Translation</b> : $sTip1ContentTranslation<br />
		<b>$sTip2Translation</b> : $sTip2ContentTranslation
		<br /><br /><input type='submit' name='features' value='$ok' /></form>";
	}
	
	/**
	 * Second step<br />
	 * Choose the post for the comment contest
	 * @param $currentPage The current page (post display)
	 * @param $ranks The ranks which are allowed to participate
	 * @param $numWinners The winners' number
	 * @param $numParticipation The participation's number for each person
	 * @param $email Email's content
	 * @param $type The contest's type
	 * @param $prizes Prizes' number
	 * @param $mailsubject [string] Email subject
	 */
	/*private */function step2_choosePost($currentPage, $ranks, $numWinners, $numParticipation, $email, $type, $prizes, $mailsubject) {
		global $wpdb;
		$maxArticles = 20;
		
		$choosePost = __("Choose a post", $this->localizationName);
		$more = __("More...", $this->localizationName);
		$noPost = __("No post found!", $this->localizationName);
		$comments = __("comment(s)", $this->localizationName);
		
		echo "<h1>Comment Contest - $choosePost</h1><form id='postForm' action='plugins.php?page=comment-contest.php' method='post'>";
		
		// V1.35 - ADD : Only display posts with comments
		$query = "SELECT * FROM $wpdb->posts WHERE (post_status = 'publish' OR post_status = 'private') AND post_type='post' AND comment_count > 0 ORDER BY post_date DESC";
		
		$posts = $wpdb->get_results ( $query );
		
		if ($posts) {
			$url = get_bloginfo ( 'url' );
			for($i = $currentPage; $i < count ( $posts ) && $i < $maxArticles + $currentPage; $i ++) {
				$post = $posts [$i];
				echo "<a href='#' onclick='getElementById(\"postnumber\").value=$post->ID; getElementById(\"postForm\").submit()'>$post->post_title</a>  ($post->comment_count $comments)<br />";
			}
			
			echo "<input type='hidden' name='rank' value='$ranks' />
			<input type='hidden' name='numWinners' value='$numWinners' />
			<input type='hidden' name='numParticipation' value='$numParticipation' />
			<input type='hidden' name='email' value='$email' />
			<input type='hidden' name='contestType' value='$type' />
			<input type='hidden' name='numPrizes' value='$prizes' />
			<input type='hidden' name='postnumber' id='postnumber' value='' />
			<input type='hidden' name='mailsubject' id='mailsubject' value='$mailsubject' />";
			
			if (count ( $posts ) > $maxArticles) {
				echo "<br /><input type='hidden' name='pagepost' value='" . ($currentPage + $maxArticles) . "' />
				<a href='#' onclick='getElementById(\"postnumber\").value=-1; getElementById(\"postForm\").submit()'>$more</a>";
			}
			
			echo "</form>";
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
	 * @param $email Email's content
	 * @param $type The contest's type
	 * @param $prizes Prizes' number
	 * @param $mailsubject [string] Email subject
	 * @param $errorMessage Error message
	 * @param $previousComments Comments previously checked
	 */
	/*private */function step3_chooseComments($idPost, $ranks, $numWinners, $numParticipation, $email, $type, $prizes, $mailsubject, $errorMessage = null, $previousComments = null) {
		global $wpdb;
		$chooseComments = __("Choose comments to include in the contest", $this->localizationName);
		$ok = __("Ok!", $this->localizationName);
		$noComment = __("No comment found!", $this->localizationName);
		
		echo "<h1>Comment Contest - $chooseComments</h1>";
		
		if($errorMessage != null) {
			echo "<div id='message' class='error'><p>$errorMessage</p></div>";
		}
		
		$filter = null;
		foreach ( explode ( ",", $ranks ) as $rank ) {
			if ($rank != - 1 && $rank != 0) {
				$queryTemp = "select user_login from $wpdb->usermeta, $wpdb->users where meta_key='wp_user_level' and  meta_value=$rank and user_id=ID";
				$resultQueryTemp = $wpdb->get_results ( $queryTemp );

				foreach ( $resultQueryTemp as $resultTemp ) {
					// V1.35 - BUG FIX : Simple and double quotes protected because if a pseudo contains quotes, the query bugs (thanks to Kamel from www.yoocom.fr)
					$filter [] = "comment_author = '" . addslashes($resultTemp->user_login) . "'";
				}
			} elseif ($rank == 0) { // Subscriber
				// V1.36 - Remove sub-query & add a new temporary query
				$subQueryTemp = "select distinct user_id from $wpdb->usermeta where meta_key='wp_user_level'";
				$resultSubQueryTemp = $wpdb->get_results ( $subQueryTemp );
				$resultTempString = null;
				foreach($resultSubQueryTemp as $r) {
					$resultTempString[] = $r->user_id;
				}
				
				//$queryTemp = "select meta_value from $wpdb->usermeta where meta_key='nickname' and user_id not in(select distinct user_id from $wpdb->usermeta where meta_key='wp_user_level')";
				$queryTemp = "select meta_value from $wpdb->usermeta where meta_key='nickname' and user_id not in(" . implode(",", $resultTempString) . ")";
				$resultQueryTemp = $wpdb->get_results ( $queryTemp );

				foreach ( $resultQueryTemp as $resultTemp ) {
					// V1.35 - BUG FIX : Simple and double quotes protected because if a pseudo contains quotes, the query bugs (thanks to Kamel from www.yoocom.fr)
					$filter [] = "comment_author = '" . addslashes($resultTemp->meta_value) . "'";
				}
			} else { // User ($rank == -1)
				// V1.36 - Remove sub-query & add a new temporary query
				$subQueryTemp = "select meta_value from $wpdb->usermeta where meta_key='nickname'";
				$resultSubQueryTemp = $wpdb->get_results ( $subQueryTemp );
				$resultTempString = null;
				foreach($resultSubQueryTemp as $r) {
					$resultTempString[] = "\"" . addslashes($r->meta_value) . "\"";
				}
				
				//$queryTemp = "SELECT comment_author FROM $wpdb->comments WHERE comment_approved = \"1\" and comment_post_id='$idPost' and comment_author not in (select meta_value from $wpdb->usermeta where meta_key='nickname')";
				$queryTemp = "SELECT comment_author FROM $wpdb->comments WHERE comment_approved = \"1\" and comment_post_id='$idPost' and comment_author not in (" . implode(",", $resultTempString) . ")";
				$resultQueryTemp = $wpdb->get_results ( $queryTemp );

				foreach ( $resultQueryTemp as $resultTemp ) {
					// V1.35 - BUG FIX : Simple and double quotes protected because if a pseudo contains quotes, the query bugs (thanks to Kamel from www.yoocom.fr)
					$filter [] = "comment_author = '" . addslashes($resultTemp->comment_author) . "'";
				}
			}
		}
		
		// V1.35 - BUG FIX : If $filter is null, a PHP error occured ==> filter management now here
		if($filter == null) {
			$filter = "";
		} else {
			$filter = " and (" . implode ( " OR ", $filter ) . ")";
		}
		
		// V1.36 - Remove sub-query & add a new temporary query
		$query = "select user_login from $wpdb->users u, $wpdb->posts p where u.ID = post_author and p.ID = '$idPost'";
		$postAuthor = $wpdb->get_results ( $query );
		$postAuthor = addslashes($postAuthor[0] -> user_login);
		
		//$query = "SELECT * FROM $wpdb->comments WHERE comment_approved = '1' and comment_post_id='$idPost' and comment_author != (select user_login from $wpdb->users u, $wpdb->posts p where u.ID = post_author and p.ID = '$idPost') $filter ORDER BY comment_author";
		$query = "SELECT * FROM $wpdb->comments WHERE comment_approved = '1' and comment_post_id='$idPost' and comment_author != '$postAuthor' $filter ORDER BY comment_author";
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
				
				if($previousComments != null) {
					if(in_array($id, $previousComments)) {
						$checked = " checked='checked'";
					} else {
						$checked = "";
					}
				}
				
				echo "<input type='checkbox' name='comments[]' value='$id'$checked /> <strong>$from : </strong>" . substr ( strip_tags ( $comment_content ), 0, 100 ) . "<br /><br />";
			}
			echo "<br/><input type='hidden' name='post' value='$idPost' />
			<input type='hidden' name='rank' value='$ranks' />
			<input type='hidden' name='numWinners' value='$numWinners' />
			<input type='hidden' name='numParticipation' value='$numParticipation' />
			<input type='hidden' name='email' value='$email' />
			<input type='hidden' name='contestType' value='$type' />
			<input type='hidden' name='numPrizes' value='$prizes' />
			<input type='hidden' name='mailsubject' value='$mailsubject' />
			<input type='submit' value='$ok' /></form>";
		} else {
			$this->error ( $noComment, array ("home") );
		}
	}
	
	/**
	 * Forth step<br />
	 * Manage the contest's prizes
	 * @param $comments The complete participants' list (created with all checkboxes)
	 * @param $numWinners The winners' number
	 * @param $numParticipation The participation's number for each person
	 * @param $email Email's content
	 * @param $type The contest's type
	 * @param $prizes Prizes' number
	 * @param $mailsubject [string] Email subject
	 * @param $error Error code
	 * @param $previousNames Prizes' names previously typed
	 * @param $previousTo Prizes' places previously typed
	 */
	function step4_choosePrizes($comments, $numWinners, $numParticipation, $email, $type, $prizes, $mailsubject, $error = 0, $previousNames = null, $previousTo = null) {
		$choosePrizes = __("Prizes' choice", $this->localizationName);
		$launchContest = __("Launch the contest", $this->localizationName);
		$prizeName = __("Prize name:", $this->localizationName);
		$start = __("From", $this->localizationName);
		$end = __("to", $this->localizationName);
		
		echo "<h1>Comment Contest - $choosePrizes</h1>";
		
		// V1.35 - ADD : Different names check and code optimization
		if($error > 0) {
			if($error == 1) {
				$errorText = __("Please give all prizes!", $this->localizationName);
			} elseif($error == 2) {
				$errorText = __("Please check all places for each prize", $this->localizationName);
			} elseif($error == 3) {
				$errorText = __("Please give different name for each prize", $this->localizationName);
			}
			
			echo "<div id='message' class='error'><p>$errorText</p></div>";
		}
			
		echo "<script type='text/javascript' src='$this->pluginDir/comment-contest.js'></script>
			<form action='plugins.php?page=comment-contest.php' method='post'>";

		if($prizes == 1) {
			echo "$prizeName <input type='text' name='prizeName' /><br />";
		} else {
			$previousNames = explode(",", $previousNames);
			$previousTo = explode(",", $previousTo);
			
			for($i = 0; $i < $prizes; $i++) {
				if($i == 0) {
					$beginNumber = 1;
				} else {
					if($previousTo != null) {
						$beginNumber = $previousTo[$i-1] + 1;
					} else {
						$beginNumber = null;
					}
				}
				
				if($i == $prizes - 1) {
					$endField = "readonly='readonly' value='$numWinners'";
				} else {
					$endField = "onkeyup='changePlace(this.value, " . ($i + 1) . ")'";
					
					if($previousTo != null) {
						$endField .= " value='$previousTo[$i]'";
					}
				}
				
				if($previousNames != null) {
					$n = " value='" . stripslashes($previousNames[$i]) . "'";
				} else {
					$n = null;
				}
				
				echo "$prizeName <input type='text' name='prizeName[]'$n />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$start <input type='text' name='from[]' readonly='readonly' value='$beginNumber' id='to$i' /> $end <input type='text' name='to[]' $endField /><br />";
			}
		}
		
		echo "<input type='hidden' name='comments' value='$comments' />
			<input type='hidden' name='numWinners' value='$numWinners' />
			<input type='hidden' name='numParticipation' value='$numParticipation' />
			<input type='hidden' name='email' value='$email' />
			<input type='hidden' name='contestType' value='$type' />
			<input type='hidden' name='mailsubject' value='$mailsubject' />
			<input type='hidden' name='numPrizes' value='$prizes' /><br />
			<input type='submit' name='prizesSubmit' value='$launchContest' /></form>";
	}
	
	/**
	 * Fifth step<br />
	 * Display winners
	 * @param $comments The complete participants' list (created with all checkboxes)
	 * @param $numWinners The winners' number
	 * @param $numParticipation The participation's number for each person
	 * @param $email Email's content
	 * @param $type The contest's type
	 * @param $prizes Prizes' names
	 * @param $places Prizes' order
	 * @param $mailsubject [string] Email subject
	 */
	/*private */function step5_displayWinners($comments, $numWinners, $numParticipation, $email, $type, $prizes, $places, $mailsubject) {
		global $wpdb;
		
		$winners = __("Winners", $this->localizationName);
		$commentWord = __("Comment", $this->localizationName);
		$say = __("says", $this->localizationName);
		$emailNotSendTranslation = __("Email cannot be sent to :", $this->localizationName);
		$winnersEmailPhraseTranslation = __("Winners emails", $this->localizationName);
		$loosersEmailPhraseTranslation = __("Loosers emails", $this->localizationName);
		
		$blogname = get_option('blogname');
		$adminEmail = get_option("admin_email");
		$siteUrl = get_option("siteurl");
		
		$message_headers = "From: $adminEmail\n"
			. "Content-Type: text/html; charset=\"" . get_option('blog_charset') . "\"\n"
			. "Reply-To: $adminEmail\n";
		
		$tab = null;
		$comments = explode(",", $comments);
		foreach ( $comments as $comment => $value ) {
			$tab [] = $value;
		}
		
		if($type == "normal") {
			shuffle ( $tab );
		}
		
		echo "<h1>Comment Contest - $winners</h1>";
		$i = 1; $k = 1;
		$author = "";
		$prizeName = __("Prize won:", $this->localizationName);
		
		if(!is_array($prizes)) {
			$sThePrize = stripslashes($prizes);
		} else {
			$sThePrize = stripslashes($prizes[0]);
		}
		
		echo "<h2>$prizeName $sThePrize</h2>";
		
		$allWinnersEmail = null;
		$allLoosersEmail = null;
		
		// V1.4 - ADD : Separate loosers and winners emails
		// BEGIN V1.4
		$sEmailAllContent = base64_decode($email);
		$asEmailAllContent = split("<!--more-->", $sEmailAllContent);
		$sEmailWinnersContent = $asEmailAllContent[0];
		$sEmailLoosersContent = $asEmailAllContent[1];
		// END V1.4
		
		// V1.37 - UPDATE : Update the whole loop to save emails of all participants, to display them later
		// V1.4 - ADD : Update the whole loop to send emails to winners and loosers
		for($j = 0; $j < count ( $tab ); $j ++) {
			$query = "SELECT * FROM $wpdb->comments WHERE comment_approved = '1' and comment_id='$tab[$j]'";
			$comment = $wpdb->get_results ( $query );
			$c = $comment [0];
			$from = $c->comment_author;
			$authorEmail = $c->comment_author_email;
			
			if ($from != $author && $i <= $numWinners) {
				$i ++;
				$author = $from;
				
				if(is_array($prizes) && $j + 1 == $places[$k]) {
					$sThePrize = stripslashes($prizes[$k++]);
					echo "<h2>$prizeName $sThePrize</h2>";
				}
				
				echo "<strong>$commentWord</strong> $from <strong>$say</strong> $c->comment_content <br /><br />";
				$allWinnersEmail[] = $authorEmail;
				
				if($email != null) {
					if(!wp_mail($authorEmail, $mailsubject, str_replace("%prize%", $sThePrize, $sEmailWinnersContent), $message_headers)) {
						echo "<br /><b>$emailNotSendTranslation $authorEmail</b><br />";
					}
				}
			}
			
			if ($j >= $numWinners) {
				$allLoosersEmail[] = $authorEmail;
				
				if($email != null) { // V1.4 - ADD : Send email to loosers
					if(!wp_mail($authorEmail, $mailsubject, $sEmailLoosersContent, $message_headers)) {
						echo "<br /><b>$emailNotSendTranslation $authorEmail</b><br />";
					}
				}
			}
		}
		
		$allParticipants = __("List of all participants:", $this->localizationName);
		echo "<br /><hr /><br />$allParticipants ";
		$tabTemp = null;
		for($j = 0; $j < count ( $tab ); $j ++) {
			$query = "SELECT comment_author FROM $wpdb->comments WHERE comment_approved = '1' and comment_id='$tab[$j]'";
			$tabTemp[] = $wpdb->get_var ( $query );
		}
		array_unique($tabTemp);
		natcasesort($tabTemp); // V1.35 - UPDATE : Remove case sensitive sort ("natcasesort($array)" replace "sort($array)")
		echo implode(", ", $tabTemp);
		
		// V1.37 - ADD : Display participants emails
		// BEGIN 1.37
		echo "<br /><hr /><b>$winnersEmailPhraseTranslation :</b> " . implode(",", $allWinnersEmail) . "<br />";
		if(count($allLoosersEmail) > 0) {
			echo "<br /><b>$loosersEmailPhraseTranslation :</b> " . implode(",", $allLoosersEmail) . "<br />";
		}
		// END 1.37
	}
	
	/**
	 * Display an error message
	 * @param $message The error message
	 * @param $args The message's parameter. <code>$args[0]</code> must be <i>"post"</i> or <i>"home"</i>
	 * @version 1.35 - UPDATE : Change the message format
	 */
	/*private */function error($message, $args) {
		$url = get_bloginfo ( 'url' );
		if ($args [0] == 'post') {
			$chooseComment = __("Choose comments", $this->localizationName);
			
			die ( "<div id='message' class='error'><p>$message</p></div><br /><br />
			<form action='plugins.php?page=comment-contest.php' method='post'>
			<input type='hidden' name='postnumber' value='$args[1]' />
			<input type='hidden' name='rank' value='$args[2]' />
			<input type='hidden' name='numWinners' value='$args[3]' />
			<input type='hidden' name='numParticipation' value='$args[4]' />
			<input type='hidden' name='email' value='$args[5]' />
			<input type='submit' value='$chooseComment' /></form>" );
		} elseif ($args [0] == 'home') {
			$back = __("Back", $this->localizationName);
			
			die ( "<div id='message' class='error'><p>$message</p></div><br /><br /><a href='$url/wp-admin/plugins.php?page=comment-contest.php'>$back</a>" );
		}
	}
	
	/**
	 * Check fields' format in the prizes' choice
	 * @param $names The prizes' names
	 * @param $to The places to go
	 * @param $from The places to start from
	 * @return The checks' result : 0 for ok, 1 for names error, 2 for "to" error
	 */
	/*private */function checkPrizes(&$names, $to, $from) {
		// Check names format
		$test = false;
		for($i = 0; $i < count($names); $i++) {
			if($names[$i] == "" || $names[$i] == null) {
				$test = true;
			}
			$names[$i] = strip_tags($names[$i]);
		}
		
		if($test) {
			return 1;
		} else {
			//Check "To" format
			$test = false;
			for($i = 0; $i < count($to) && !$test; $i++) {
				if(($to[$i] == "") ||
						($i < count($to) - 1 && $to[$i] > $to[$i + 1]) ||
						($from[$i] > $to[$i])) {
					$test = true;
				}
			}
			
			if($test) {
				return 2;
			} else {
				return 0; // Everything is fine
			}
		}
	}
	
	/**
	 * The page to display in the administration menu
	 */
	function AdminHelpPage() {
		if(isset($_POST['prizesSubmit'])) { // Step 5 : Display winners
			
			$res = $this->checkPrizes($_POST['prizeName'], $_POST['to'], $_POST['from']);
			
			// V1.35 - ADD : Check different names
			if(is_array($_POST['prizeName'])) {
				$temp = $_POST['prizeName'];
				$temp = array_unique($temp);
				if(count($temp) != count($_POST['prizeName'])) {
					$res = 3;
				}
			}

			if($res == 0) {
				$this->step5_displayWinners ( $_POST ['comments'], $_POST ['numWinners'], $_POST ['numParticipation'], $_POST ['email'], $_POST['contestType'], $_POST['prizeName'], $_POST['from'], $_POST['mailsubject'] );
			} else {
				$this->step4_choosePrizes($_POST ['comments'], $_POST ['numWinners'], $_POST ['numParticipation'], $_POST ['email'], $_POST['contestType'], $_POST['numPrizes'], $_POST['mailsubject'], $res, implode(",", $_POST['prizeName']), implode(",", $_POST['to']));
			}

// ---------------------------------------------------------------------------------
			
		}
		else if (isset ( $_POST ['postnumber'] ) && $_POST ['postnumber'] != -1) { // Step 3 : Choose comments
			$this->step3_chooseComments ( $_POST ['postnumber'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'], $_POST ['email'], $_POST['contestType'], $_POST['numPrizes'], $_POST["mailsubject"] );

// ---------------------------------------------------------------------------------

		} elseif (isset ( $_POST ['post'] ) && $_POST ['postnumber'] != -1) { // Step 4 : Choose prizes
			$comments = $_POST ['comments'];
			
			if ($comments == null || count ( $comments ) == 0) {
				$selectOneWinner = __("Please select one winner at least!", $this->localizationName);
				$this->step3_chooseComments ( $_POST ['post'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'], $_POST ['email'], $_POST['contestType'], $_POST['numPrizes'], $_POST['mailsubject'], $selectOneWinner, null );
			} elseif (count ( $comments ) < $_POST ['numWinners']) {
				$selectMoreWinner = __("Please select more participants than winners!", $this->localizationName);
				$this->step3_chooseComments ( $_POST ['post'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'], $_POST ['email'], $_POST['contestType'], $_POST['numPrizes'], $_POST['mailsubject'], $selectMoreWinner, $comments );
			} else {
				$this->step4_choosePrizes(implode(",", $comments), $_POST ['numWinners'], $_POST ['numParticipation'], $_POST ['email'], $_POST['contestType'], $_POST['numPrizes'], $_POST['mailsubject']);
			}
// ---------------------------------------------------------------------------------

		} elseif (isset ( $_POST ['features'] ) || isset($_POST ['pagepost'])) { // Step 2 : Choose an article
			$page = (isset ( $_POST ['pagepost'] ) ? intval ( $_POST ['pagepost'] ) : 0);
			$page = ($page > 0 ? $page : 0);
			
			// Check list
			$numWinners = intval ( $_POST ['numWinners'] );
			$numParticipation = intval ( $_POST ['numParticipation'] );
			$numPrizes = intval ( $_POST ['numPrizes'] );
			
			if(isset($_POST['sendmail'])) {
				$email = base64_encode($_POST['mailcontent']); // V1.36 - UPDATE : Better protection for this field
			} else {
				$email = null;
			}
			
			$mailsubject = strip_tags($_POST["mailsubject"]);
			
			$emailContentTest = base64_encode(addslashes(__("Email's content", $this->localizationName)));

			$asEmailAllContent = split("<!--more-->", $_POST['mailcontent']);
			$sEmailWinnersContent = $asEmailAllContent[0];
			$sEmailLoosersContent = $asEmailAllContent[1];
				
			if (count ( $_POST ['rank'] ) == 0) {
				$selectOneRank = __("Please select one rank at least!", $this->localizationName);
				$this->step1_configure ($selectOneRank, $_POST['contestType'], $numWinners, $numParticipation, $numPrizes, null, $email, $mailsubject);
			} elseif ($numWinners == null || $numWinners <= 0) {
				$winnerFormat = __("Wrong winners format!", $this->localizationName);
				$this->step1_configure ($winnerFormat, $_POST['contestType'], $numWinners, $numParticipation, $numPrizes, $_POST ['rank'], $email, $mailsubject);
			} elseif ($numParticipation == null || $numParticipation <= 0) {
				$participationsFormat = __("Wrong participations format!", $this->localizationName);
				$this->step1_configure ($participationsFormat, $_POST['contestType'], $numWinners, $numParticipation, $numPrizes, $_POST ['rank'], $email, $mailsubject);
			} elseif(isset($_POST['sendmail']) && ($email == $emailContentTest || $mailsubject == null || $mailsubject == "")) {
				$emailContentError = __("Please change the email's content!", $this->localizationName);
				$this->step1_configure ($emailContentError, $_POST['contestType'], $numWinners, $numParticipation, $numPrizes, $_POST ['rank'], $email, $mailsubject);
			} elseif(isset($_POST['sendmail']) && (strpos($_POST['mailcontent'], "<!--more-->") === false || $sEmailWinnersContent == null || $sEmailWinnersContent == "" || $sEmailLoosersContent == null || $sEmailLoosersContent == "")) {
				$sEmailContentMoreErrorTranslation = __("Please put a \"More\" tag and a message for the loosers!", $this->localizationName);
				$this->step1_configure ($sEmailContentMoreErrorTranslation, $_POST['contestType'], $numWinners, $numParticipation, $numPrizes, $_POST ['rank'], $email, $mailsubject);
			} elseif ($numPrizes == null || $numPrizes <= 0 || $numPrizes > $numWinners) {
				$prizesFormat = __("Wrong prizes number format! Or choose more winners than prizes!", $this->localizationName);
				$this->step1_configure ($prizesFormat, $_POST['contestType'], $numWinners, $numParticipation, $numPrizes, $_POST ['rank'], $email, $mailsubject);
			} else {
				if(is_array($_POST ['rank'])) {
					$tab = implode ( ",", $_POST ['rank'] );
				} else {
					$tab = $_POST ['rank'];
				}
				$this->step2_choosePost ( $page, $tab, $numWinners, $numParticipation, $email, $_POST['contestType'], $numPrizes, $mailsubject );
			}
			
// ---------------------------------------------------------------------------------

		} else { // Step 1 : Configure the contest
			$this->step1_configure ();
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