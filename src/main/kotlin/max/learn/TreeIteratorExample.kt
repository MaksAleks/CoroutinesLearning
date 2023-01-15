@file:Suppress("UNUSED", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package max.learn

import kotlinx.coroutines.*

val context = newSingleThreadContext("my-thread")
fun main(): Unit = runBlocking(context) {

    val root = TreeNode(
        "1",
        TreeNode(
            "2",
            TreeNode("4"),
            TreeNode("5")
        ),
        TreeNode("6")
    )

    val iterator = TreeNodeIterator(root, this)

    while (iterator.hasNext()) {
        println(iterator.next())
    }
}

class TreeNode(val data: String, val left: TreeNode? = null, val right: TreeNode? = null)

class TreeNodeIterator(val root: TreeNode, private val coroutineScope: CoroutineScope) {
    private var data: String? = null
    private var walker: Job? = null

    suspend fun hasNext(): Boolean {
        if (walker == null) {
            walker = coroutineScope.launch {
                treeWalk(root)
            }
        }
        yield()
        return walker?.isCompleted != true
    }

    suspend fun next(): String? {
        var res: String? = data
        if (res == null) {
            yield()
            res = data
        }
        data = null
        return res
    }

    suspend fun treeWalk(node: TreeNode?) {
        if (node?.left != null) {
            treeWalk(node.left)
        }
        data = node?.data

        yield()

        if (node?.right != null) {
            treeWalk(node.right)
        }
    }
}

