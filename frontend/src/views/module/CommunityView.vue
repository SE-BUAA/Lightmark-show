<template>
  <div class="module-page">
    <section class="community-hero">
      <div class="container hero-inner">
        <div>
          <span class="module-kicker">旅行社区</span>
          <h1 class="section-title">分享路线、提问攻略、收藏灵感</h1>
          <p class="section-subtitle">发布游记、参与评论，也可以在问答区向其他旅行者寻求建议。</p>
        </div>
        <el-button type="primary" size="large" @click="openCreatePost">发布游记</el-button>
      </div>
    </section>

    <section class="community-section">
      <div class="container">
        <el-tabs v-model="activeTab" class="community-tabs">
          <el-tab-pane label="游记" name="posts">
            <div class="toolbar">
              <el-input v-model="postKeyword" placeholder="搜索游记" clearable @keyup.enter="loadPosts" />
              <el-select v-model="postSort" @change="loadPosts">
                <el-option label="最新" value="latest" />
                <el-option label="热门" value="hot" />
              </el-select>
              <el-button @click="loadPosts">搜索</el-button>
            </div>

            <el-empty v-if="posts.length === 0" description="暂无游记" />
            <div v-else class="post-grid">
              <article v-for="post in posts" :key="post.id" class="post-card">
                <img v-if="firstImage(post.images)" :src="firstImage(post.images)" :alt="post.title" />
                <div class="post-body">
                  <h2>{{ post.title }}</h2>
                  <p>{{ post.content }}</p>
                  <div class="post-meta">
                    <span>发布者：{{ displayNickname(post.user_nickname, post.user_id) }}</span>
                    <span>{{ formatTime(post.create_time) }}</span>
                    <span>{{ post.comments_count }} 评论</span>
                  </div>
                  <div class="post-actions">
                    <el-button text @click="openPostDetail(post)">详情</el-button>
                    <el-button
                      text
                      :type="post.liked ? 'success' : 'primary'"
                      :class="{ 'liked-action': post.liked }"
                      @click="handleLike(post)"
                    >
                      {{ post.liked ? '已赞' : '点赞' }} · {{ post.likes }}
                    </el-button>
                    <el-button text @click="openComments(post)">评论</el-button>
                    <el-button v-if="canEditPost(post)" text @click="openEditPost(post)">编辑</el-button>
                    <el-button v-if="canDeletePost(post)" text type="danger" @click="handleDeletePost(post)">删除</el-button>
                  </div>
                </div>
              </article>
            </div>
          </el-tab-pane>

          <el-tab-pane label="问答" name="questions">
            <div class="toolbar">
              <el-input v-model="questionKeyword" placeholder="搜索问题" clearable @keyup.enter="loadQuestions" />
              <el-button @click="loadQuestions">搜索</el-button>
              <el-button type="primary" @click="showQuestionDialog = true">提问</el-button>
            </div>

            <el-empty v-if="questions.length === 0" description="暂无问题" />
            <div v-else class="question-list">
              <article v-for="question in questions" :key="question.id" class="question-card">
                <div>
                  <h2>{{ question.title }}</h2>
                  <p>{{ question.content }}</p>
                  <div class="question-meta">
                    <span>提问者：{{ displayNickname(question.user_nickname, question.user_id) }}</span>
                    <span>{{ formatTime(question.create_time) }}</span>
                  </div>
                  <div v-if="question.answer" class="answer-box">
                    <div class="answer-meta">回答者：{{ displayNickname(question.answer_user_nickname, question.answer_user_id) }}</div>
                    {{ question.answer }}
                  </div>
                </div>
                <div class="question-side">
                  <el-tag :type="question.status === 1 ? 'success' : 'info'">{{ question.status === 1 ? '已回答' : '待回答' }}</el-tag>
                  <el-button size="small" @click="openAnswer(question)">回答</el-button>
                </div>
              </article>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </section>

    <button class="floating-publish" type="button" @click="openCreatePost">+</button>

    <el-dialog v-model="showPostDialog" :title="editingPost?.id ? '编辑游记' : '发布游记'" width="560px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="postForm.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="图片地址">
          <el-input v-model="postForm.images" placeholder='例如 ["https://picsum.photos/id/104/640/420"]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" :loading="posting" @click="handleSubmitPost">{{ editingPost?.id ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPostDetailDialog" :title="postDetail?.title || '游记详情'" width="720px">
      <div v-if="postDetail" class="post-detail">
        <img v-if="firstImage(postDetail.images)" :src="firstImage(postDetail.images)" :alt="postDetail.title" />
        <div class="post-detail-meta">
          <span>发布者：{{ displayNickname(postDetail.user_nickname, postDetail.user_id) }}</span>
          <span>{{ formatTime(postDetail.create_time) }}</span>
          <span>{{ postDetail.likes }} 赞</span>
          <span>{{ postDetail.comments_count }} 评论</span>
        </div>
        <p>{{ postDetail.content }}</p>
      </div>
      <template #footer>
        <el-button @click="showPostDetailDialog = false">关闭</el-button>
        <el-button v-if="postDetail" @click="openComments(postDetail)">查看评论</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCommentsDialog" :title="selectedPost?.title || '评论'" width="620px">
      <div class="comment-list">
        <el-empty v-if="comments.length === 0" description="暂无评论" />
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-heading">
            <strong>评论者：{{ displayNickname(comment.user_nickname, comment.user_id) }}</strong>
            <el-button v-if="canDeleteComment(comment)" text type="danger" @click="handleDeleteComment(comment)">删除</el-button>
          </div>
          <p>{{ comment.content }}</p>
        </div>
      </div>
      <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="写下你的评论" />
      <template #footer>
        <el-button @click="showCommentsDialog = false">关闭</el-button>
        <el-button type="primary" :loading="commenting" @click="handleCreateComment">发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showQuestionDialog" title="发布问题" width="560px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="questionForm.title" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="questionForm.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showQuestionDialog = false">取消</el-button>
        <el-button type="primary" :loading="questioning" @click="handleCreateQuestion">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAnswerDialog" title="回答问题" width="560px">
      <p class="answer-title">{{ selectedQuestion?.title }}</p>
      <el-input v-model="answerContent" type="textarea" :rows="5" placeholder="输入回答内容" />
      <template #footer>
        <el-button @click="showAnswerDialog = false">取消</el-button>
        <el-button type="primary" :loading="answering" @click="handleAnswerQuestion">提交回答</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import {
  answerQuestion,
  deletePost,
  createPost,
  createQuestion,
  createPostComment,
  deletePostComment,
  getPost,
  getPostComments,
  getPosts,
  getQuestions,
  togglePostLike,
  updatePost,
  type Comment,
  type Post,
  type Question
} from '@/api/community'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const activeTab = ref('posts')
const posts = ref<Post[]>([])
const questions = ref<Question[]>([])
const comments = ref<Comment[]>([])
const postKeyword = ref('')
const postSort = ref('latest')
const questionKeyword = ref('')
const showPostDialog = ref(false)
const showPostDetailDialog = ref(false)
const showCommentsDialog = ref(false)
const showQuestionDialog = ref(false)
const showAnswerDialog = ref(false)
const selectedPost = ref<Post | null>(null)
const editingPost = ref<Post | null>(null)
const postDetail = ref<Post | null>(null)
const selectedQuestion = ref<Question | null>(null)
const commentContent = ref('')
const answerContent = ref('')
const posting = ref(false)
const commenting = ref(false)
const questioning = ref(false)
const answering = ref(false)

const postForm = reactive({
  title: '',
  content: '',
  images: ''
})

const questionForm = reactive({
  title: '',
  content: ''
})

const loadPosts = async () => {
  const page = await getPosts({ page: 1, size: 20, keyword: postKeyword.value, sort: postSort.value })
  posts.value = page.list || []
}

const loadQuestions = async () => {
  const page = await getQuestions({ page: 1, size: 20, keyword: questionKeyword.value })
  questions.value = page.list || []
}

const openCreatePost = () => {
  editingPost.value = null
  Object.assign(postForm, { title: '', content: '', images: '' })
  showPostDialog.value = true
}

const openEditPost = (post: Post) => {
  if (!canEditPost(post)) return
  editingPost.value = { ...post }
  Object.assign(postForm, {
    title: post.title,
    content: post.content,
    images: post.images || ''
  })
  showPostDialog.value = true
}

const handleSubmitPost = async () => {
  if (!postForm.title.trim() || !postForm.content.trim()) {
    ElMessage.warning('请填写标题和正文')
    return
  }
  if (!isValidImages(postForm.images)) {
    ElMessage.warning('图片地址需要是 JSON 数组，例如 ["https://..."]')
    return
  }
  posting.value = true
  try {
    const isEditing = Boolean(editingPost.value?.id)
    const payload = {
      title: postForm.title,
      content: postForm.content,
      images: postForm.images || '[]',
      likes: editingPost.value?.likes || 0,
      comments_count: editingPost.value?.comments_count || 0
    }
    if (isEditing && editingPost.value?.id) {
      await updatePost(editingPost.value.id, payload)
    } else {
      await createPost(payload)
    }
    Object.assign(postForm, { title: '', content: '', images: '' })
    editingPost.value = null
    showPostDialog.value = false
    await loadPosts()
    ElMessage.success(isEditing ? '游记已更新' : '游记已发布')
  } finally {
    posting.value = false
  }
}

const openPostDetail = async (post: Post) => {
  if (!post.id) return
  postDetail.value = await getPost(post.id)
  showPostDetailDialog.value = true
}

const handleDeletePost = async (post: Post) => {
  if (!post.id || !canDeletePost(post)) return
  await ElMessageBox.confirm('确认删除这篇游记？', '删除游记', { type: 'warning' })
  await deletePost(post.id)
  await loadPosts()
  ElMessage.success('游记已删除')
}

const handleLike = async (post: Post) => {
  if (!post.id) return
  const result = await togglePostLike(post.id)
  post.likes = result.likes
  post.liked = result.liked
  if (postDetail.value?.id === post.id) {
    postDetail.value.likes = result.likes
    postDetail.value.liked = result.liked
  }
  ElMessage.success(result.liked ? '已点赞' : '已取消点赞')
}

const openComments = async (post: Post) => {
  if (!post.id) return
  selectedPost.value = post
  commentContent.value = ''
  const page = await getPostComments(post.id, { page: 1, size: 50 })
  comments.value = page.list || []
  showPostDetailDialog.value = false
  showCommentsDialog.value = true
}

const handleCreateComment = async () => {
  if (!selectedPost.value?.id || !commentContent.value.trim()) return
  commenting.value = true
  try {
    await createPostComment(selectedPost.value.id, { content: commentContent.value })
    selectedPost.value.comments_count += 1
    await openComments(selectedPost.value)
    ElMessage.success('评论已发送')
  } finally {
    commenting.value = false
  }
}

const handleDeleteComment = async (comment: Comment) => {
  if (!selectedPost.value?.id || !comment.id || !canDeleteComment(comment)) return
  await ElMessageBox.confirm('确认删除这条评论？', '删除评论', { type: 'warning' })
  await deletePostComment(selectedPost.value.id, comment.id)
  selectedPost.value.comments_count = Math.max((selectedPost.value.comments_count || 0) - 1, 0)
  await openComments(selectedPost.value)
  await loadPosts()
  ElMessage.success('评论已删除')
}

const handleCreateQuestion = async () => {
  if (!questionForm.title.trim() || !questionForm.content.trim()) {
    ElMessage.warning('请填写问题标题和描述')
    return
  }
  questioning.value = true
  try {
    await createQuestion({ title: questionForm.title, content: questionForm.content })
    Object.assign(questionForm, { title: '', content: '' })
    showQuestionDialog.value = false
    await loadQuestions()
    ElMessage.success('问题已发布')
  } finally {
    questioning.value = false
  }
}

const openAnswer = (question: Question) => {
  selectedQuestion.value = question
  answerContent.value = question.answer || ''
  showAnswerDialog.value = true
}

const handleAnswerQuestion = async () => {
  if (!selectedQuestion.value?.id || !answerContent.value.trim()) return
  answering.value = true
  try {
    await answerQuestion(selectedQuestion.value.id, answerContent.value)
    showAnswerDialog.value = false
    await loadQuestions()
    ElMessage.success('回答已提交')
  } finally {
    answering.value = false
  }
}

const firstImage = (images?: string) => {
  if (!images) return ''
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? String(parsed[0] || '') : ''
  } catch {
    return ''
  }
}

const isValidImages = (images: string) => {
  if (!images.trim()) return true
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed)
  } catch {
    return false
  }
}

const formatTime = (value?: string) => {
  return value ? value.slice(0, 10) : '刚刚'
}

const displayNickname = (nickname?: string, fallbackId?: string) => {
  return nickname?.trim() || (fallbackId ? `用户${fallbackId}` : '-')
}

const isOwner = (userId?: string) => {
  return Boolean(userId && authStore.userId && String(userId) === String(authStore.userId))
}

const canEditPost = (post: Post) => {
  return isOwner(post.user_id)
}

const canDeletePost = (post: Post) => {
  return authStore.isAdmin || isOwner(post.user_id)
}

const canDeleteComment = (comment: Comment) => {
  return authStore.isAdmin || isOwner(comment.user_id)
}

onMounted(async () => {
  authStore.hydrate()
  await Promise.all([loadPosts(), loadQuestions()])
})
</script>

<style scoped>
.module-page { padding-top: 64px; }
.community-hero {
  background: linear-gradient(135deg, #7f3f2f, #c45d42 48%, #c9953d);
  color: #fff;
  padding: 48px 0 36px;
}
.community-hero .section-title,
.community-hero .section-subtitle { color: #fff; }
.hero-inner {
  align-items: center;
  display: flex;
  gap: 24px;
  justify-content: space-between;
}
.module-kicker {
  color: var(--gold-200);
  display: block;
  font-weight: 700;
  margin-bottom: 10px;
}
.community-section { padding: 30px 0 56px; }
.community-tabs {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 18px;
}
.toolbar {
  display: grid;
  gap: 12px;
  grid-template-columns: 1fr 140px auto;
  margin-bottom: 18px;
}
.post-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}
.post-card,
.question-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  overflow: hidden;
}
.post-card img {
  aspect-ratio: 16 / 9;
  object-fit: cover;
  width: 100%;
}
.post-body {
  padding: 16px;
}
.post-body h2,
.question-card h2 {
  font-size: 18px;
  margin-bottom: 8px;
}
.post-body p,
.question-card p {
  color: var(--text-secondary);
}
.post-meta,
.question-meta,
.post-actions {
  align-items: center;
  color: var(--text-secondary);
  display: flex;
  font-size: 13px;
  gap: 14px;
  justify-content: space-between;
  margin-top: 12px;
}
.post-actions {
  flex-wrap: wrap;
  justify-content: flex-start;
}
.liked-action {
  font-weight: 700;
}
.question-meta {
  flex-wrap: wrap;
  justify-content: flex-start;
}
.post-detail img {
  aspect-ratio: 16 / 9;
  border-radius: 8px;
  object-fit: cover;
  width: 100%;
}
.post-detail-meta {
  color: var(--text-secondary);
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin: 14px 0;
}
.post-detail p {
  color: var(--text-primary);
  line-height: 1.8;
  white-space: pre-wrap;
}
.question-list {
  display: grid;
  gap: 14px;
}
.question-card {
  align-items: flex-start;
  display: flex;
  justify-content: space-between;
  padding: 18px;
}
.question-side {
  align-items: flex-end;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.answer-box {
  background: var(--cream-100);
  border-left: 3px solid var(--accent);
  color: var(--text-primary);
  margin-top: 12px;
  padding: 12px;
}
.answer-meta {
  color: var(--text-secondary);
  font-size: 13px;
  margin-bottom: 6px;
}
.comment-list {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
  max-height: 320px;
  overflow: auto;
}
.comment-item {
  border-bottom: 1px solid var(--border-light);
  padding-bottom: 10px;
}
.comment-heading {
  align-items: center;
  display: flex;
  justify-content: space-between;
}
.comment-item p {
  color: var(--text-secondary);
  margin-top: 4px;
}
.answer-title {
  color: var(--text-secondary);
  margin-bottom: 12px;
}
.floating-publish {
  align-items: center;
  background: var(--accent);
  border: 0;
  border-radius: 50%;
  bottom: 28px;
  box-shadow: var(--shadow-md);
  color: #fff;
  cursor: pointer;
  display: flex;
  font-size: 30px;
  font-weight: 300;
  height: 56px;
  justify-content: center;
  position: fixed;
  right: 28px;
  width: 56px;
  z-index: 8;
}
@media (max-width: 760px) {
  .hero-inner,
  .question-card {
    align-items: flex-start;
    flex-direction: column;
  }
  .toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
