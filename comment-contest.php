<?php
/*
Plugin Name: Comment Contest
Plugin URI: http://www.nozzhy.com
Description: If you create a contest on your website, you can draw all comments in a specific post (only in French for now)
Author: Thomas "Zhykos" Cicognani
Version: 1.0
Author URI: http://www.nozzhy.com
*/

/**
 * Manage the comments' contest
 * @author Thomas "Zhykos" Cicognani
 * @see www.nozzhy.com
 */
class CommentContest {
	var $domain = '';
	var $version = '1.0'; //Changer pour correspondre à la version courante
	var $option_ns = '';
	var $options = array ();
	
	// Raccourci interne pour ajouter des actions
	function add_action($nom, $num = 0) {
		$hook = $nom;
		$fonction = $nom;
		if (! $num) {
			$fonction .= $num;
		}
		add_action ( $hook, array (&$this, 'action_' . $nom ) );
	}
	
	function CommentContest() {
		// Initialisation des variables
		if ($this->domain == '')
			$this->domain = get_class ( $this );
		if ($this->option_ns == '')
			$this->option_ns = get_class ( $this );
			// Récupération des options
		$this->options = get_option ( $this->option_ns );
		
		// Doit-on lancer l'installation ?
		if (! isset ( $this->options ['install'] ) or ($this->options ['install'] != $this->version))
			$this->install ();
			
		//Charger les données de localisation
		load_plugin_textdomain ( $this->domain );
		
		// gestion automatique des actions
		foreach ( get_class_methods ( get_class ( $this ) ) as $methode ) {
			if (substr ( $methode, 0, 7 ) == 'action_') {
				$this->add_action ( substr ( $methode, 7 ) );
			}
		}
	
	}
	
	function action_admin_menu() {
		if (function_exists ( 'add_submenu_page' )) {
			add_submenu_page ( 'plugins.php', __ ( 'Comment Contest', $this->domain ), __ ( 'Comment Contest', $this->domain ), 3, basename ( __FILE__ ), array (&$this, 'AdminHelpPage' ) );
		}
	}
	
	function set($option, $value) {
		$this->options [$option] = $value;
	}
	
	function get($option) {
		if (isset ( $this->options [$option] )) {
			return $this->options [$option];
		} else {
			return false;
		}
	}
	
	function update_options() {
		return update_option ( $this->option_ns, $this->options );
	}
	
	//---------------------------------------------
	// Editez à partir d'ici
	//---------------------------------------------
	

	function install() {
		// Fonction permettant l'installation de votre plugin (création de tables, de paramètres...)
		$this->set ( 'install', $this->version );
		$this->set ( 'page', 0 );
		$this->update_options ();
	}
	
	/**
	 * First step<br />
	 * Configure the contest's settings
	 */
	private function configure() {
		echo "<h1>Comment Contest - Configurations du tirage au sort</h1>
		<form action='plugins.php?page=comment-contest.php' method='post'>
		Nombre de gagnants : <input type='text' name='winners' value='2' /><br />
		Nombre maximum de participations par personne : <input type='text' name='number' value='1' /><br />
		<table>
			<tr style='vertical-align: top'>
				<td>Rangs autoris&eacute;s pour participation :</td>
				<td>
					<input type='checkbox' name='rank[]' value='10' /> Administrateur<br />
					<input type='checkbox' name='rank[]' value='7' /> &Eacute;diteur<br />
					<input type='checkbox' name='rank[]' value='2' /> Auteur<br />
					<input type='checkbox' name='rank[]' value='1' /> Contributeur<br />
					<input type='checkbox' name='rank[]' value='0' /> Abonn&eacute;<br />
					<input type='checkbox' name='rank[]' value='-1' checked='checked' /> Utilisateur normal
				</td>
			</tr>
		</table>
		<br /><input type='submit' name='features' value='Ok !' /></form>";
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
		echo '<h1>Comment Contest - Choisir un article</h1><form id="postForm" action="plugins.php?page=comment-contest.php" method="post">';
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
				echo "<br /><a href='$url/wp-admin/plugins.php?page=comment-contest.php&amp;pagepost=" . ($currentPage + $maxArticles) . "'>Plus...</a>";
			}
		} else {
			$this->error ( "Aucun article !", array ("home" ) );
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
		
		echo "<h1>Comment Contest - Choisir les commentaires &agrave; inclure dans le concours</h1>";
		
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
			<input type='submit' value='Lancer le tirage au sort' /></form>";
		} else {
			$this->error ( "Aucun commentaire !", array ("home" ) );
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
		
		$tab = null;
		foreach ( $comments as $comment => $value ) {
			$tab [] = $value;
		}
		
		shuffle ( $tab );
		echo "<h1>Comment Contest - Les gagnants</h1>";
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
				echo "<strong>Commentaire de</strong> $from <strong>avec</strong> $c->comment_content <br /><br />";
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
			die ( "$message<br /><br />
			<form action='plugins.php?page=comment-contest.php' method='post'>
			<input type='hidden' name='postnumber' value='$args[1]' />
			<input type='hidden' name='rank' value='$args[2]' />
			<input type='hidden' name='numWinners' value='$args[3]' />
			<input type='hidden' name='numParticipation' value='$args[4]' />
			<input type='submit' value='Rechoisir les commentaires' /></form>" );
		} elseif ($args [0] == 'home') {
			die ( "$message<br /><br /><a href='$url/wp-admin/plugins.php?page=comment-contest.php'>Retour au d&eacute;but</a>" );
		}
	}
	
	function AdminHelpPage() {
		if (isset ( $_POST ['postnumber'] )) { // Step 3 : Choose comments
			$this->chooseComments ( $_POST ['postnumber'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'] );
		} elseif (isset ( $_POST ['post'] )) { // Step 4 : Display winners
			$comments = $_POST ['comments'];
			
			if ($comments == null || count ( $comments ) == 0) {
				$this->error ( "Veuillez s&eacute;lectionner au moins un gagnant !", array ("post", $_POST ['post'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'] ) );
			} elseif (count ( $comments ) <= $_POST ['numWinners']) {
				$this->error ( "Veuillez s&eacute;lectionner plus de participants que de gagnants !", array ("post", $_POST ['post'], $_POST ['rank'], $_POST ['numWinners'], $_POST ['numParticipation'] ) );
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
				$this->error ( "Veuillez s&eacute;lectionner au moins un rang !", array ("home" ) );
			} elseif ($numWinners == null || $numWinners <= 0) {
				$this->error ( "Veuillez sp&eacute;cifier un nombre correct de gagnants !", array ("home" ) );
			} elseif ($numParticipation == null || $numParticipation <= 0) {
				$this->error ( "Veuillez sp&eacute;cifier un nombre correct de participations !", array ("home" ) );
			} else {
				$this->choosePost ( $page, implode ( ",", $_POST ['rank'] ), $numWinners, $numParticipation );
			}
		} else { // Step 1 : Configure the contest
			$this->configure ();
		}
	}
	
	function action_wp_title($titre) {
		return $titre;
	}
	
//---------------------------------------------
// Fin de la partie d'édition
//---------------------------------------------


}

$inst_CommentContest = new CommentContest ( );

?>